import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testobject.TestObject

Mobile.callTestCase(findTestCase('Test Cases/Android/Authentication/TC_LOGIN'), [:], FailureHandling.STOP_ON_FAILURE)

Mobile.tap(findTestObject('Android/Bill Payment/btn_pay_bill'), 60)

Mobile.tap(findTestObject('Android/Bill Payment/radio_pay_in_full'), 60)


TestObject toggle = findTestObject('Android/Bill Payment/toggle_recurring_payment')
if (Mobile.verifyElementExist(toggle, 5, FailureHandling.OPTIONAL)) {
	String isOn = Mobile.getAttribute(toggle, 'checked', 10)
	if (isOn == 'true') {
		Mobile.tap(toggle, 60)
	}
}

Mobile.tap(findTestObject('Android/Bill Payment/btn_yes_continue'), 60)

Mobile.tap(findTestObject('Android/Bill Payment/card_saved_payment_method'), 60)

Mobile.tap(findTestObject('Android/Bill Payment/payment_method_okay'), 60)

Mobile.setText(findTestObject('Android/Bill Payment/input_saved_card_cvv'), '321', 60)

Mobile.hideKeyboard()

boolean successShown = Mobile.verifyElementExist(findTestObject('Android/Bill Payment/txt_payment_success'), 30, FailureHandling.OPTIONAL)

assert successShown : 'Payment success screen did not appear within 30 seconds.'

