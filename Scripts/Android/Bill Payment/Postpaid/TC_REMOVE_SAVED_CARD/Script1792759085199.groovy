import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testobject.TestObject

Mobile.callTestCase(findTestCase('Android/Authentication/TC_LOGIN'), [:], FailureHandling.STOP_ON_FAILURE)
Mobile.tap(findTestObject('Android/Bill Payment/btn_pay_bill'), 60)
Mobile.tap(findTestObject('Android/Bill Payment/radio_pay_in_full'), 60)
Mobile.setText(findTestObject('Android/Bill Payment/input_payment_amount'), '12', 60)
if (Mobile.verifyElementExist(findTestObject('Android/Bill Payment/toggle_recurring_payment'), 5, FailureHandling.OPTIONAL)) {
	Mobile.tap(findTestObject('Android/Bill Payment/toggle_recurring_payment'), 60)
}
Mobile.hideKeyboard()
Mobile.tap(findTestObject('Android/Bill Payment/btn_yes_continue'), 60)
if (!Mobile.verifyElementExist(findTestObject('Android/Bill Payment/btn_manage_payment_methods'), 3, FailureHandling.OPTIONAL)) {
	try {
		Mobile.enhancedScrollToText('Manage Payment Methods', 60)
	} catch (Exception ignored) {
		// No scrollable container present; element wasn't found, so nothing to scroll to.
	}
}
Mobile.tap(findTestObject('Android/Bill Payment/btn_manage_payment_methods'), 60)
Mobile.tap(findTestObject('Android/Bill Payment/icon_delete_card'), 60)
Mobile.tap(findTestObject('Android/Bill Payment/btn_confirm_delete_card'), 60)
Mobile.tap(findTestObject('Android/Bill Payment/btn_close_delete_confirmation'), 60)

boolean savedCardRemoved = Mobile.verifyElementNotExist(
	findTestObject('Android/Bill Payment/card_saved_payment_method'),
	30,
	FailureHandling.OPTIONAL)
assert savedCardRemoved : 'The saved card is still visible after confirming its deletion.'
