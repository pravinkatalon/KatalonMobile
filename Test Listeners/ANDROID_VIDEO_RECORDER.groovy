import java.util.concurrent.TimeUnit

import com.kms.katalon.core.annotation.AfterTestCase
import com.kms.katalon.core.annotation.BeforeTestCase
import com.kms.katalon.core.annotation.BeforeTestSuite
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.context.TestCaseContext
import com.kms.katalon.core.context.TestSuiteContext
import com.kms.katalon.core.util.KeywordUtil

class ANDROID_VIDEO_RECORDER {

    private static final long COMMAND_TIMEOUT_SECONDS = 15
    private static final long RECORDER_STOP_TIMEOUT_SECONDS = 10
    private static final int VIDEO_BIT_RATE = 4_000_000
    private static final int VIDEO_TIME_LIMIT_SECONDS = 180
    private static final String VIDEO_SIZE = '720x1280'

    private Process recordingProcess
    private String recordingPid
    private String recordingOwner
    private String remoteVideoPath
    private String adbExecutable
    private String deviceId
    private int testCaseNumber

    @BeforeTestSuite
    def beforeTestSuite(TestSuiteContext testSuiteContext) {
        testCaseNumber = 0
        try {
            ensureRecorderConfiguration()
        } catch (IOException exception) {
            KeywordUtil.markWarning("Android video recording is unavailable: ${exception.message}")
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt()
            KeywordUtil.markWarning('Android video recorder setup was interrupted')
        }
    }

    @BeforeTestCase
    def beforeTestCase(TestCaseContext testCaseContext) {
        if (recordingOwner != null) {
            return
        }

        testCaseNumber++
        recordingOwner = testCaseContext.getTestCaseId()

        String safeTestCaseName = sanitizeFileName(recordingOwner)
        remoteVideoPath = "/sdcard/katalon_${String.format('%03d', testCaseNumber)}_${safeTestCaseName}.mp4"

        try {
            ensureRecorderConfiguration()
            cleanupOrphanedRecordings()
            assertRecorderIsAvailable()
            recordingProcess = new ProcessBuilder(
                adbExecutable,
                '-s',
                deviceId,
                'shell',
                'screenrecord',
                '--bit-rate',
                VIDEO_BIT_RATE.toString(),
                '--size',
                VIDEO_SIZE,
                '--time-limit',
                VIDEO_TIME_LIMIT_SECONDS.toString(),
                remoteVideoPath
            )
                .redirectErrorStream(true)
                .start()

            consumeOutput(recordingProcess)
            Thread.sleep(1000)

            if (!recordingProcess.isAlive()) {
                throw new IOException("Android screenrecord exited before ${recordingOwner} started")
            }

            recordingPid = findRecorderPid()
            if (!recordingPid) {
                throw new IOException("Unable to identify Android screenrecord for ${recordingOwner}")
            }

            KeywordUtil.logInfo("Recording ${recordingOwner} to ${remoteVideoPath}")
        } catch (IOException exception) {
            resetRecordingState()
            KeywordUtil.markWarning("Unable to start Android video recording: ${exception.message}")
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt()
            resetRecordingState()
            KeywordUtil.markWarning('Android video recording was interrupted before it started')
        }
    }

    @AfterTestCase
    def afterTestCase(TestCaseContext testCaseContext) {
        if (recordingOwner == null || testCaseContext.getTestCaseId() != recordingOwner) {
            return
        }

        String completedTestCase = recordingOwner
        int completedTestCaseNumber = testCaseNumber

        try {
            stopDeviceRecorder()

            File recordingDirectory = getRecordingDirectory()
            String safeTestCaseName = sanitizeFileName(completedTestCase)
            String timestamp = new Date().format('yyyyMMdd_HHmmss_SSS')
            File localVideo = new File(
                recordingDirectory,
                "${String.format('%03d', completedTestCaseNumber)}_${safeTestCaseName}_${timestamp}.mp4"
            )

            runCommand([
                adbExecutable,
                '-s',
                deviceId,
                'pull',
                remoteVideoPath,
                localVideo.absolutePath
            ])
            runCommand([
                adbExecutable,
                '-s',
                deviceId,
                'shell',
                'rm',
                '-f',
                remoteVideoPath
            ])

            if (!localVideo.isFile() || localVideo.length() == 0) {
                throw new IOException("The recording for ${completedTestCase} was not saved")
            }

            KeywordUtil.logInfo("Saved Android recording: ${localVideo.absolutePath}")
        } catch (IOException exception) {
            KeywordUtil.markWarning("Unable to save Android video recording: ${exception.message}")
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt()
            KeywordUtil.markWarning('Android video recording was interrupted while saving')
        } finally {
            resetRecordingState()
        }
    }

    private void stopDeviceRecorder() throws IOException, InterruptedException {
        if (recordingPid) {
            runCommand([
                adbExecutable,
                '-s',
                deviceId,
                'shell',
                'kill',
                '-2',
                recordingPid
            ], false)
        }

        if (recordingProcess != null &&
            !recordingProcess.waitFor(RECORDER_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            recordingProcess.destroy()
            recordingProcess.waitFor(RECORDER_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
    }

    private File getRecordingDirectory() throws IOException {
        String reportFolder = RunConfiguration.getReportFolder()
        File reportDirectory = reportFolder ? new File(reportFolder) : null

        if (reportDirectory != null && !reportDirectory.isAbsolute()) {
            reportDirectory = new File(RunConfiguration.getProjectDir(), reportFolder)
        }

        File directory = reportDirectory
            ? new File(reportDirectory, 'recordings')
            : new File(RunConfiguration.getProjectDir(), 'Reports/recordings')

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create recording directory ${directory.absolutePath}")
        }

        return directory
    }

    private String findAdbExecutable() {
        List<File> candidates = []
        String androidSdkRoot = System.getenv('ANDROID_SDK_ROOT')
        String androidHome = System.getenv('ANDROID_HOME')

        if (androidSdkRoot) {
            candidates.add(new File(androidSdkRoot, 'platform-tools/adb'))
        }
        if (androidHome) {
            candidates.add(new File(androidHome, 'platform-tools/adb'))
        }

        candidates.add(new File(
            System.getProperty('user.home'),
            '.katalon/tools/android_sdk/platform-tools/adb'
        ))

        File installedAdb = candidates.find { it.isFile() && it.canExecute() }
        return installedAdb?.absolutePath ?: 'adb'
    }

    private void ensureRecorderConfiguration() throws IOException, InterruptedException {
        if (!adbExecutable) {
            adbExecutable = findAdbExecutable()
        }
        if (!deviceId) {
            deviceId = findSingleConnectedDevice()
            KeywordUtil.logInfo("Android video recording enabled for ${deviceId}")
        }
    }

    private String findSingleConnectedDevice() throws IOException, InterruptedException {
        String output = runCommand([adbExecutable, 'devices'])
        List<String> connectedDevices = output
            .readLines()
            .drop(1)
            .findAll { it.endsWith('\tdevice') }
            .collect { it.split(/\s+/)[0] }

        if (connectedDevices.size() != 1) {
            throw new IOException(
                "Expected one connected Android device, found ${connectedDevices.size()}: ${connectedDevices}"
            )
        }

        return connectedDevices.first()
    }

    private void assertRecorderIsAvailable() throws IOException, InterruptedException {
        if (findRecorderPid()) {
            throw new IOException(
                "Another Android screenrecord process is already running on ${deviceId}"
            )
        }
    }

    private void cleanupOrphanedRecordings() throws IOException, InterruptedException {
        String recorderPids = runCommand([
            adbExecutable,
            '-s',
            deviceId,
            'shell',
            'pidof',
            'screenrecord'
        ], false).trim()

        recorderPids
            .split(/\s+/)
            .findAll { it ==~ /\d+/ }
            .each { recorderPid ->
                String commandLine = runCommand([
                    adbExecutable,
                    '-s',
                    deviceId,
                    'shell',
                    'cat',
                    "/proc/${recorderPid}/cmdline"
                ], false)

                if (commandLine.contains('/sdcard/katalon_')) {
                    runCommand([
                        adbExecutable,
                        '-s',
                        deviceId,
                        'shell',
                        'kill',
                        '-2',
                        recorderPid
                    ], false)
                }
            }

        String remoteFiles = runCommand([
            adbExecutable,
            '-s',
            deviceId,
            'shell',
            'ls',
            '/sdcard'
        ], false)

        remoteFiles
            .readLines()
            .findAll { it ==~ /katalon_[A-Za-z0-9._-]+\.mp4/ }
            .each { orphanedFile ->
                runCommand([
                    adbExecutable,
                    '-s',
                    deviceId,
                    'shell',
                    'rm',
                    '-f',
                    "/sdcard/${orphanedFile}"
                ], false)
            }
    }

    private String findRecorderPid() throws IOException, InterruptedException {
        String output = runCommand([
            adbExecutable,
            '-s',
            deviceId,
            'shell',
            'pidof',
            'screenrecord'
        ], false).trim()

        return output
            .split(/\s+/)
            .find { it ==~ /\d+/ }
    }

    private String runCommand(List<String> command, boolean failOnError = true)
        throws IOException, InterruptedException {
//        Process process = new ProcessBuilder(command)
			Process process = new ProcessBuilder(command.collect { it.toString() })
            .redirectErrorStream(true)
            .start()

        boolean completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (!completed) {
            process.destroy()
            throw new IOException("Command timed out: ${command.join(' ')}")
        }
        String output = process.inputStream.getText('UTF-8')
        if (failOnError && process.exitValue() != 0) {
            throw new IOException(
                "Command failed (${process.exitValue()}): ${command.join(' ')}\n${output.trim()}"
            )
        }

        return output
    }

    private void consumeOutput(Process process) {
        Thread.startDaemon('android-screenrecord-output') {
            process.inputStream.withReader('UTF-8') { reader ->
                while (reader.readLine() != null) {
                    // Drain ADB output so a long recording cannot block on a full buffer.
                }
            }
        }
    }

    private String sanitizeFileName(String value) {
        return value
            .tokenize('/')
            .last()
            .replaceAll('[^A-Za-z0-9._-]', '_')
    }

    private void resetRecordingState() {
        recordingProcess = null
        recordingPid = null
        recordingOwner = null
        remoteVideoPath = null
    }
}
