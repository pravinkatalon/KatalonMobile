import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.configuration.RunConfiguration as RunConfiguration
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling

import internal.GlobalVariable as GlobalVariable

String configuredAppPath = GlobalVariable.G_AppPath?.toString()?.trim()
String selectedMsisdn = msisdn?.toString()?.trim()
String selectedPassword = password?.toString()

if (!selectedMsisdn) {
	selectedMsisdn = GlobalVariable.G_MSISDN?.toString()?.trim()
}
if (selectedPassword == null || selectedPassword.isEmpty()) {
	selectedPassword = GlobalVariable.G_Password?.toString()
}

assert configuredAppPath : 'G_AppPath must be configured in the selected execution profile.'
assert selectedMsisdn : 'An MSISDN must be supplied or configured as G_MSISDN.'
assert selectedPassword : 'A password must be supplied or configured as G_Password.'

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
Mobile.setText(findTestObject('Android/Authentication/input_msisdn'), selectedMsisdn, 60)
Mobile.hideKeyboard()
Mobile.tap(findTestObject('Android/Authentication/android.widget.Button - Continue'), 60)

Mobile.tap(findTestObject('Android/Authentication/btn_use_password'), 60)

if (!Mobile.verifyElementExist(findTestObject('Android/Authentication/input_password'), 5, FailureHandling.OPTIONAL)) {
	Mobile.delay(1)
	Mobile.tap(findTestObject('Android/Authentication/btn_use_password'), 60)
	boolean passwordModeActive = Mobile.verifyElementExist(findTestObject('Android/Authentication/input_password'), 10, FailureHandling.OPTIONAL)
	assert passwordModeActive : 'Tapping "Use Password Instead" did not switch the login screen into password mode.'
}

Mobile.tap(findTestObject('Android/Authentication/forgot_password_link'), 60)
Mobile.tap(findTestObject('Android/Authentication/fpt'), 60)
Mobile.sendKeys(findTestObject('Android/Authentication/fpt'), '18762340000')
Mobile.hideKeyboard()

String enteredValue = Mobile.getText(findTestObject('Android/Authentication/fpt'), 10)
assert enteredValue?.contains('18762340000') : "Expected the field to contain the typed MSISDN, but got: ${enteredValue}"

Mobile.tap(findTestObject('Android/Authentication/request_reset_btn'), 60)

if (!Mobile.verifyElementExist(findTestObject('Android/Authentication/invalid_msisdn'), 10, FailureHandling.OPTIONAL)) {
	Mobile.delay(1)
	Mobile.tap(findTestObject('Android/Authentication/request_reset_btn'), 60)
}

boolean msisdnLengthErrorShown = Mobile.verifyElementExist(findTestObject('Android/Authentication/invalid_msisdn'), 60, FailureHandling.OPTIONAL)
assert msisdnLengthErrorShown : 'Expected the "mobile number must have..." validation error to appear for an invalid MSISDN length.'

Mobile.closeApplication()