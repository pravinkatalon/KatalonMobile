import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling

import internal.GlobalVariable as GlobalVariable
import com.kms.katalon.core.testobject.TestObject

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

Mobile.tap(findTestObject('Android/Bill Payment/tab_home_entertainment'), 60)

String accountName = GlobalVariable.G_HomeAccount?.toString()?.trim()
assert accountName : 'G_HomeAccount must be configured in the selected execution profile.'

int maxSwipes = 5
int swipeCount = 0
while (!Mobile.verifyElementExist(findTestObject('Android/Bill Payment/mobile_account_tap', [('accountName') : accountName]), 2, FailureHandling.OPTIONAL) && swipeCount < maxSwipes) {
	Mobile.swipe(360, 1300, 360, 600)
	swipeCount++
}
Mobile.tap(findTestObject('Android/Bill Payment/mobile_account_tap', [('accountName') : accountName]), 60)
Mobile.tap(findTestObject('Android/Bill Payment/btn_pay_bill'), 60)
TestObject toggle = findTestObject('Android/Bill Payment/toggle_recurring_payment')
if (Mobile.verifyElementExist(toggle, 5, FailureHandling.OPTIONAL)) {
	String isOn = Mobile.getAttribute(toggle, 'checked', 10)
	if (isOn == 'true') {
		Mobile.tap(toggle, 60)
	}
}
Mobile.tap(findTestObject('Android/Bill Payment/radio_pay_in_full'), 60)
Mobile.tap(findTestObject('Android/Bill Payment/btn_yes_continue'), 60)

boolean alreadyPaidShown = Mobile.verifyElementExist(
	findTestObject('Android/Bill Payment/txt_bill_already_paid'),
	20,
	FailureHandling.OPTIONAL)
assert alreadyPaidShown : "Expected 'Your bill was already paid!' message did not appear."