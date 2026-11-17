import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import internal.GlobalVariable as GlobalVariable

String msisdn = GlobalVariable.G_MSISDN?.toString()?.trim()
String password = GlobalVariable.G_Password?.toString()
Boolean useOtp = GlobalVariable.G_UseOTP

assert msisdn : 'G_MSISDN must be configured for this test case.'
assert useOtp != null : 'G_UseOTP must be configured for this test case.'
assert useOtp || password :
	'G_Password must be configured when G_UseOTP is false.'

Mobile.callTestCase(
	findTestCase('Test Cases/Android/Authentication/TC_LOGIN'),
	[
		('msisdn') : msisdn,
		('password') : password,
		('useOtp') : useOtp
	],
	FailureHandling.STOP_ON_FAILURE)

Mobile.tap(findTestObject('Android/Bill Payment/tab_mobile'), 60)

String accountName = GlobalVariable.G_AlternateMSISDN?.toString()?.trim()

assert accountName : 'G_AlternateMSISDN must be configured in the selected execution profile.'

int maxSwipes = 5

int swipeCount = 0

while (!(Mobile.verifyElementExist(findTestObject('Android/Bill Payment/mobile_account_tap', [('accountName') : accountName]),
	2, FailureHandling.OPTIONAL)) && (swipeCount < maxSwipes)) {
	Mobile.swipe(360, 1300, 360, 600)

	swipeCount++
}

Mobile.tap(findTestObject('Android/Bill Payment/mobile_account_tap', [('accountName') : accountName]), 60)

Mobile.delay(3)

Mobile.tap(findTestObject('Android/Account/back_btn'), 60)

Mobile.delay(2)

for (int i = 0; i < swipeCount; i++) {
	Mobile.swipe(360, 600, 360, 1300)
}

boolean accountNameMatches = Mobile.verifyElementExist(
	findTestObject('Android/Bill Payment/selected_account_confirm', [('accountName') : accountName]),
	30,
	FailureHandling.OPTIONAL
)
assert accountNameMatches : "Expected the account shown on screen to match '${accountName}', but it did not."

Mobile.delay(3)

Mobile.closeApplication()