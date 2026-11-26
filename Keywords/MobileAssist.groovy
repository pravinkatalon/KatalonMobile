import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.mobile.keyword.internal.MobileDriverFactory
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.util.KeywordUtil
import io.appium.java_client.AppiumDriver
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

import java.time.Duration
import java.util.concurrent.TimeUnit

class MobileAssist {

    /** Default swipe length as a fraction of the container height (80% -> 20%). */
    private static final double DEFAULT_SWIPE_SCALE = 0.6

    /**
     * Scrolls until text is visible or maxSwipes is reached.
     * swipeScale: fraction of the container height each swipe covers (e.g. 0.3 for short swipes).
     */
    @Keyword
    static boolean scrollToText(TestObject container, String text, String direction = 'DOWN',
                                int maxSwipes = 20, boolean exactMatch = false,
                                double swipeScale = DEFAULT_SWIPE_SCALE) {
        validateArguments(text, direction, maxSwipes)
        validateSwipeScale(swipeScale)

        AppiumDriver driver = MobileDriverFactory.getDriver()
        Map scrollTarget = resolveScrollTarget(driver, container)

        if (scrollTarget == null) {
            return false
        }

        for (int attempt = 0; attempt <= maxSwipes; attempt++) {
            boolean found = attempt == 0
                    ? containsTextInitial(driver, text, exactMatch, scrollTarget.element as WebElement)
                    : containsText(driver, text, exactMatch, scrollTarget.element as WebElement)
            if (found) {
                println "MobileAssist found text '${text}' after ${attempt} scroll(s)"
                return true
            }

            if (attempt < maxSwipes) {
                if (!scroll(scrollTarget, direction, swipeScale)) { 
                    return false
                }
                Mobile.delay(1)
            }
        }

        println "MobileAssist could not find text '${text}' after ${maxSwipes} scroll(s)"
        return false
    }

    /** Scrolls page by page until text is visible or the page limit is reached. */
    @Keyword
    static boolean smartScrollToText(TestObject container, String text, String direction = 'DOWN',
                                     int pages = 5, boolean exactMatch = false,
                                     double swipeScale = DEFAULT_SWIPE_SCALE) {
        validateArguments(text, direction, pages)
        validateSwipeScale(swipeScale)

        AppiumDriver driver = MobileDriverFactory.getDriver()
        Map scrollTarget = resolveScrollTarget(driver, container)

        if (scrollTarget == null) {
            return false
        }

        for (int page = 0; page <= pages; page++) {
            boolean found = page == 0
                    ? containsTextInitial(driver, text, exactMatch, scrollTarget.element as WebElement)
                    : containsText(driver, text, exactMatch, scrollTarget.element as WebElement)
            if (found) {
                println "MobileAssist found text '${text}' after ${page} page scroll(s)"
                return true
            }

            if (page < pages) {
                if (!scroll(scrollTarget, direction, swipeScale)) {
                    return false
                }
                Mobile.delay(1)
            }
        }

        println "MobileAssist could not find text '${text}' after ${pages} page scroll(s)"
        return false
    }

    /** Seconds the first check keeps re-reading the current view before the first swipe. */
    private static final int INITIAL_SETTLE_SECONDS = 3

    /**
     * First check on the current view. The screen may still be rendering right after the
     * previous step, so re-read it every 500ms for up to INITIAL_SETTLE_SECONDS before swiping.
     */
    private static boolean containsTextInitial(AppiumDriver driver, String text, boolean exactMatch, WebElement container = null) {
        long deadline = System.currentTimeMillis() + INITIAL_SETTLE_SECONDS * 1000L
        while (true) {
            if (containsText(driver, text, exactMatch, container)) {
                return true
            }
            if (System.currentTimeMillis() >= deadline) {
                println "MobileAssist text '${text}' not in current view after ${INITIAL_SETTLE_SECONDS}s, starting to scroll"
                return false
            }
            Thread.sleep(500)
        }
    }

    /**
     * Searches for the text nested anywhere inside the container (or the whole screen when there is none).
     * Only items the list has rendered are in the tree, which is why the caller swipes between checks.
     */
    private static boolean containsText(AppiumDriver driver, String text, boolean exactMatch, WebElement container = null) {
        String literal = xpathLiteral(text)
        String textCondition = exactMatch ? "@text = ${literal}" : "contains(@text, ${literal})"
        String descriptionCondition = exactMatch ? "@content-desc = ${literal}" : "contains(@content-desc, ${literal})"
        String labelCondition = exactMatch ? "@label = ${literal}" : "contains(@label, ${literal})"
        String nameCondition = exactMatch ? "@name = ${literal}" : "contains(@name, ${literal})"
        String valueCondition = exactMatch ? "@value = ${literal}" : "contains(@value, ${literal})"
        String condition = "${textCondition} or ${descriptionCondition} or ${labelCondition} or ${nameCondition} or ${valueCondition}"

        // Katalon leaves a 60s implicit wait after its own steps; set 0 so a miss returns at once.
        driver.manage().timeouts().implicitlyWait(Duration.ZERO)

        if (container == null) {
            List<WebElement> matches = driver.findElements(By.xpath("//*[${condition}]"))
            println "MobileAssist text search: text='${text}', inContainer=false, matches=${matches.size()}"
            return !matches.isEmpty()
        }

        // 1. Nested search from the container element.
        List<WebElement> nested = container.findElements(By.xpath(".//*[${condition}]"))
        if (!nested.isEmpty()) {
            println "MobileAssist text search: text='${text}', nested matches=${nested.size()}"
            return true
        }

        // 2. Element-scoped xpath can miss nodes on UiAutomator2: search the whole screen
        //    and keep matches whose centre lies inside the container's visible bounds.
        def box = container.getRect()
        List<WebElement> onScreen = driver.findElements(By.xpath("//*[${condition}]"))
        int inBounds = onScreen.count { WebElement el ->
            def r = el.getRect()
            int cx = r.x + (r.width / 2) as int
            int cy = r.y + (r.height / 2) as int
            cx >= box.x && cx <= box.x + box.width && cy >= box.y && cy <= box.y + box.height
        } as int
        println "MobileAssist text search: text='${text}', nested matches=0, screen matches=${onScreen.size()}, inside container=${inBounds}"
        return inBounds > 0
    }

    private static Map resolveScrollTarget(AppiumDriver driver, TestObject container) {
        def screen = driver.manage().window().size

        if (container == null) {
            Map target = [left: 0, top: 0, width: screen.width as int, height: screen.height as int]
            println "MobileAssist using full-screen swipe bounds=${target}"
            return target
        }

        List<WebElement> elements = Mobile.findElements(container, 10)

        if (elements == null || elements.isEmpty()) {
            failAndPrint("The scroll container was not found: ${container}")
            return null
        }

        WebElement element = elements[0]
        String scrollable = element.getAttribute('scrollable')
        def location = element.location
        def size = element.size

        int left = Math.max(0, location.x as int)
        int top = Math.max(0, location.y as int)
        int right = Math.min(screen.width as int, (location.x + size.width) as int)
        int bottom = Math.min(screen.height as int, (location.y + size.height) as int)
        int width = right - left
        int height = bottom - top

        if (width <= 0 || height <= 0) {
            failAndPrint("The scroll container has invalid visible bounds: [left:${left}, top:${top}, right:${right}, bottom:${bottom}]")
            return null
        }

        Map target = [left: left, top: top, width: width, height: height, element: element]
        println "MobileAssist scroll container: class=${element.getAttribute('className')}, scrollable=${scrollable}, bounds=${target}"
        return target
    }

    private static boolean scroll(Map target, String direction, double swipeScale = DEFAULT_SWIPE_SCALE) {
        int left = target.left as int
        int top = target.top as int
        int width = target.width as int
        int height = target.height as int

        if (width <= 0 || height <= 0) {
            failAndPrint("The scroll target has invalid dimensions: ${target}")
            return false
        }

        // Vertical swipe centred in the target, covering swipeScale of its height.
        // UP means bottom-to-top; DOWN means top-to-bottom.
        int startX = left + (width / 2)
        int centerY = top + (height / 2) as int
        int halfDistance = (height * swipeScale / 2) as int
        int lowerY = centerY + halfDistance
        int upperY = centerY - halfDistance
        int startY = direction.equalsIgnoreCase('UP') ? lowerY : upperY
        int endY = direction.equalsIgnoreCase('UP') ? upperY : lowerY

        println "MobileAssist explicit swipe: direction=${direction}, scale=${swipeScale}, start=(${startX},${startY}), end=(${startX},${endY}), target=${target}"

        try {
            // Mobile.swipe works locally and on Test Cloud; adb is a local-only fallback,
            // used when Mobile.swipe throws or leaves the screen unchanged.
            AppiumDriver driver = MobileDriverFactory.getDriver()
            String before = driver.getPageSource()
            boolean moved
            try {
                Mobile.swipe(startX, startY, startX, endY)
                Mobile.delay(1)
                moved = driver.getPageSource() != before
                println "MobileAssist Mobile.swipe completed, screen changed=${moved}"
            } catch (Exception swipeError) {
                println "MobileAssist Mobile.swipe failed (${swipeError.message})"
                moved = false
            }
            if (!moved) {
                println 'MobileAssist falling back to adb swipe'
                adbSwipe(startX, startY, startX, endY, 500)
                Mobile.delay(1)
                println 'MobileAssist adb swipe completed'
            }
            return true
        } catch (Exception e) {
            String message = "Unable to swipe the target: ${e.class.simpleName}: ${e.message}"
            failAndPrint(message)
            return false
        }
    }

    /** Swipes with `adb shell input swipe` on the device under test. */
    private static void adbSwipe(int x1, int y1, int x2, int y2, int durationMs) {
        List<String> command = [findAdb()]
        String serial = deviceSerial()
        if (serial) {
            command += ['-s', serial]
        }
        command += ['shell', 'input', 'swipe', "${x1}", "${y1}", "${x2}", "${y2}", "${durationMs}"].collect { it.toString() }

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start()
        String output = process.inputStream.text.trim()
        if (!process.waitFor(15, TimeUnit.SECONDS) || process.exitValue() != 0) {
            throw new IllegalStateException("adb swipe failed: ${command.join(' ')} -> ${output}")
        }
    }

    private static String deviceSerial() {
        def caps = MobileDriverFactory.getDriver().capabilities
        return (caps.getCapability('udid') ?: caps.getCapability('appium:udid') ?: caps.getCapability('deviceUDID'))?.toString()
    }

    private static String findAdb() {
        List<String> sdkRoots = [System.getenv('ANDROID_HOME'), System.getenv('ANDROID_SDK_ROOT'),
                                 "${System.getProperty('user.home')}/Library/Android/sdk".toString()]
        for (String root : sdkRoots) {
            if (root) {
                File adb = new File(root, 'platform-tools/adb')
                if (adb.canExecute()) {
                    return adb.absolutePath
                }
            }
        }
        return 'adb'
    }

    private static void failAndPrint(String message) {
        println message
        KeywordUtil.markFailed(message)
    }

    private static void validateSwipeScale(double swipeScale) {
        if (swipeScale <= 0 || swipeScale > 0.9) {
            throw new IllegalArgumentException('swipeScale must be greater than 0 and at most 0.9')
        }
    }

    private static void validateArguments(String text, String direction, int limit) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException('text must not be blank')
        }
        if (!(direction?.toUpperCase() in ['UP', 'DOWN'])) {
            throw new IllegalArgumentException("direction must be either 'UP' or 'DOWN'")
        }
        if (limit < 0) {
            throw new IllegalArgumentException('The scroll limit must be zero or greater')
        }
    }

    private static String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'${value}'"
        }
        if (!value.contains('"')) {
            return '"' + value + '"'
        }

        String[] parts = value.split("'", -1)
        return 'concat(' + parts.collect { "'${it}'" }.join(', "\'", ') + ')'
    }
}
