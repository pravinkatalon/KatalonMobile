import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.configuration.RunConfiguration as RunConfiguration
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling

import internal.GlobalVariable as GlobalVariable

String configuredAppPath = GlobalVariable.G_AppPath?.toString()?.trim()
assert configuredAppPath : 'G_AppPath must be configured in the selected execution profile.'

File appFile = new File(configuredAppPath)
if (!appFile.isAbsolute()) {
	appFile = new File(RunConfiguration.getProjectDir(), configuredAppPath)
}

String appName = appFile.name.toLowerCase()
assert appName.endsWith('.apk') : 'G_AppPath must point to an Android APK.'
assert appFile.exists() : "Application artifact not found: ${appFile.canonicalPath}"

Mobile.startApplication(appFile.canonicalPath, true)

if (Mobile.verifyElementExist(findTestObject('Android/Authentication/btn_skip'), 5, FailureHandling.OPTIONAL)) {
	Mobile.tap(findTestObject('Android/Authentication/btn_skip'), 60)
}

Mobile.tap(findTestObject('Android/Authentication/btn_accept'), 60)
Mobile.tap(findTestObject('Android/Authentication/btn_ok'), 60)
Mobile.tap(findTestObject('Android/Authentication/btn_log_in'), 60)

if (Mobile.verifyElementExist(findTestObject('Android/Authentication/input_msisdn'), 5, FailureHandling.OPTIONAL)) {
	String selectedMsisdn = msisdn?.toString()?.trim()
	if (!selectedMsisdn) {
		selectedMsisdn = GlobalVariable.G_MSISDN?.toString()?.trim()
	}
	assert selectedMsisdn : 'An MSISDN must be supplied or configured as G_MSISDN.'
	Mobile.setText(findTestObject('Android/Authentication/input_msisdn'), selectedMsisdn, 60)
	Mobile.hideKeyboard()
	Mobile.tap(findTestObject('Android/Authentication/android.widget.Button - Continue'), 60)
	Mobile.delay(5)
	Mobile.openNotifications()

	String otpNotification = Mobile.getText(findTestObject('Android/Authentication/txt_otp_notification'), 15)
	Mobile.closeNotifications()

	def otpMatch = (otpNotification =~ /\d{4}/)
	assert otpMatch.find() : 'No four-digit OTP was found in the notification.'
	String otpCode = otpMatch.group()

	Mobile.setText(findTestObject('Android/Authentication/input_otp_digit_1'), otpCode[0], 15)
	Mobile.setText(findTestObject('Android/Authentication/input_otp_digit_2'), otpCode[1], 15)
	Mobile.setText(findTestObject('Android/Authentication/input_otp_digit_3'), otpCode[2], 15)
	Mobile.setText(findTestObject('Android/Authentication/input_otp_digit_4'), otpCode[3], 15)
	Mobile.hideKeyboard()
	Mobile.tap(findTestObject('Android/Authentication/btn_submit_otp'), 60)
} else {
	println('Already logged in; skipping OTP flow.')
}
