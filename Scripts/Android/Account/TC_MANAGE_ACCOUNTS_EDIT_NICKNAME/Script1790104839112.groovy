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

import internal.GlobalVariable as GlobalVariable

String alternateMsisdn = GlobalVariable.G_AlternateMSISDN?.toString()?.trim()
String alternatePassword = GlobalVariable.G_AlternatePassword?.toString()
Boolean alternateUseOtp = GlobalVariable.G_AlternateUseOTP

assert alternateMsisdn : 'G_AlternateMSISDN must be configured for TC_BILL_ALREADY_PAID.'
assert alternateUseOtp != null : 'G_AlternateUseOTP must be configured for TC_BILL_ALREADY_PAID.'
assert alternateUseOtp || alternatePassword :
	'G_AlternatePassword must be configured when G_AlternateUseOTP is false.'

Mobile.callTestCase(
	findTestCase('Test Cases/Android/Authentication/TC_LOGIN'),
	[
		('msisdn') : alternateMsisdn,
		('password') : alternatePassword,
		('useOtp') : alternateUseOtp
	],
	FailureHandling.STOP_ON_FAILURE)

Mobile.tap(findTestObject('accounts_tab'), 60)

Mobile.tap(findTestObject('Android/Account/pencil_edit_icon'), 60)

Mobile.tap(findTestObject('Android/Account/edit_nickname_btn'), 60)

String enteredNickname = 'Test Nickname'
Mobile.setText(findTestObject('Android/Account/nickname_text_field'), enteredNickname, 60)

Mobile.tap(findTestObject('Android/Account/nickname_confirm'), 60)

boolean nicknameUpdated = Mobile.verifyElementExist(
	findTestObject('Android/Account/nickname_display', [('nickname') : enteredNickname]),
	30,
	FailureHandling.OPTIONAL
)
assert nicknameUpdated : "Expected to see the updated nickname '${enteredNickname}' displayed after confirming, but it was not found anywhere on screen."

Mobile.closeApplication()