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
Mobile.setText(findTestObject('Android/Authentication/input_password'), selectedPassword, 60)
Mobile.hideKeyboard()
Mobile.tap(findTestObject('Android/Authentication/btn_submit_password'), 60)
