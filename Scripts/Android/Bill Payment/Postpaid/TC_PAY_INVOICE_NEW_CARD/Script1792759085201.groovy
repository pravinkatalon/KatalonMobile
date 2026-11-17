import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import com.kms.katalon.core.testobject.TestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling

import internal.GlobalVariable as GlobalVariable

import java.util.concurrent.ThreadLocalRandom

String enteredAmount = BigDecimal.valueOf(
	ThreadLocalRandom.current().nextLong(100, 1100),
	2).toPlainString()

Mobile.callTestCase(findTestCase('Test Cases/Android/Authentication/TC_LOGIN'), [:], FailureHandling.STOP_ON_FAILURE)
Mobile.tap(findTestObject('Android/Bill Payment/btn_pay_bill'), 60)
Mobile.tap(findTestObject('Android/Bill Payment/radio_pay_in_full'), 60)
//Mobile.setText(findTestObject('Android/Bill Payment/input_payment_amount'), enteredAmount, 60)
TestObject toggle = findTestObject('Android/Bill Payment/toggle_recurring_payment')
if (Mobile.verifyElementExist(toggle, 5, FailureHandling.OPTIONAL)) {
	String isOn = Mobile.getAttribute(toggle, 'checked', 10)
	if (isOn == 'true') {
		Mobile.tap(toggle, 60)
	}
}
Mobile.hideKeyboard()
Mobile.tap(findTestObject('Android/Bill Payment/btn_yes_continue'), 60)

if (!Mobile.verifyElementExist(
	findTestObject('Android/Bill Payment/btn_add_payment_method'),
	3,
	FailureHandling.OPTIONAL)) {
	Mobile.enhancedScrollToText('Add New Payment Method', 60)
}

Mobile.tap(findTestObject('Android/Bill Payment/btn_add_payment_method'), 60)
Mobile.tap(findTestObject('Android/Bill Payment/card_new_payment_method'), 60)
Mobile.tap(findTestObject('Android/Bill Payment/btn_continue_new_card'), 60)
Mobile.setText(findTestObject('Android/Bill Payment/input_cardholder_name'), 'Test', 60)
Mobile.hideKeyboard()
Mobile.setText(findTestObject('Android/Bill Payment/input_card_number'), '4012000000020071', 60)
Mobile.hideKeyboard()
Mobile.setText(findTestObject('Android/Bill Payment/input_card_expiry'), '1235', 60)
Mobile.hideKeyboard()
Mobile.setText(findTestObject('Android/Bill Payment/input_new_card_cvv'), '123', 60)
Mobile.hideKeyboard()
Mobile.setText(findTestObject('Android/Bill Payment/input_billing_email'), 'test@gmail.com', 60)
Mobile.hideKeyboard()
String billingPhone = GlobalVariable.G_MSISDN?.toString()?.trim()
assert billingPhone : 'G_MSISDN must be configured in the selected execution profile.'
Mobile.setText(findTestObject('Android/Bill Payment/input_billing_phone'), billingPhone, 60)
Mobile.hideKeyboard()
Mobile.tap(findTestObject('Android/Bill Payment/btn_confirm_payment'), 60)

boolean successShown = Mobile.verifyElementExist(
	findTestObject('Android/Bill Payment/txt_payment_success'),
	30,
	FailureHandling.OPTIONAL)
assert successShown : 'Payment success screen did not appear within 30 seconds.'