import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testng.keyword.TestNGBuiltinKeywords as TestNGKW
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys


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
Mobile.clearText(findTestObject('Android/Authentication/fpt'), 60)
Mobile.sendKeys(findTestObject('Android/Authentication/fpt'), selectedMsisdn)
Mobile.hideKeyboard()

String enteredValue = Mobile.getText(findTestObject('Android/Authentication/fpt'), 10)
assert enteredValue == selectedMsisdn : "Expected the field to contain '${selectedMsisdn}' (${selectedMsisdn.length()} digits), but got '${enteredValue}' (${enteredValue?.length()} digits)"

Mobile.tap(findTestObject('Android/Authentication/request_reset_btn'), 60)

Mobile.setText(findTestObject('Android/Authentication/input_otp_digit_1'), '1', 15)
Mobile.setText(findTestObject('Android/Authentication/input_otp_digit_2'), '1', 15)
Mobile.setText(findTestObject('Android/Authentication/input_otp_digit_3'), '2', 15)
Mobile.setText(findTestObject('Android/Authentication/input_otp_digit_4'), '2', 15)

Mobile.hideKeyboard()

Mobile.tap(findTestObject('Android/Authentication/reset_password_verify'), 60)

boolean invalidCodeShown = Mobile.verifyElementExist(findTestObject('Android/Authentication/invalid_otp_code'), 30, FailureHandling.OPTIONAL)
assert invalidCodeShown : 'Expected "The provided code is incorrect. Please try again" message to appear for an invalid OTP.'

Mobile.closeApplication()

