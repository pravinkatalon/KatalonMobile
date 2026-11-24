import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import java.util.concurrent.ThreadLocalRandom as ThreadLocalRandom
import com.kms.katalon.core.testobject.TestObject

String enteredAmount = binding.hasVariable('paymentAmount') ? paymentAmount.toString().trim() : ''

if (!(enteredAmount)) {
	enteredAmount = BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(100, 1100), 2).toPlainString()
}

Mobile.callTestCase(findTestCase('Android/Authentication/TC_LOGIN'), [:], FailureHandling.STOP_ON_FAILURE)

Mobile.tap(findTestObject('Android/Bill Payment/btn_pay_bill'), 60)

Mobile.tap(findTestObject('Android/Bill Payment/radio_pay_in_full'), 60)

Mobile.setText(findTestObject('Android/Bill Payment/input_payment_amount'), enteredAmount, 60)

TestObject toggle = findTestObject('Android/Bill Payment/toggle_recurring_payment')
if (Mobile.verifyElementExist(toggle, 5, FailureHandling.OPTIONAL)) {
	String isOn = Mobile.getAttribute(toggle, 'checked', 10)
	if (isOn != 'true') {
		Mobile.tap(toggle, 60)
	}
}

Mobile.tap(findTestObject('Android/Bill Payment/btn_yes_continue'), 60)

Mobile.tap(findTestObject('Android/Bill Payment/autopay_agree_button'), 60)

boolean duplicateShown = Mobile.verifyElementExist(
	findTestObject('Android/Bill Payment/duplicate_schedule_error_message'),
	30,
	FailureHandling.OPTIONAL)
assert duplicateShown : 'Duplicate schedule error message was not displayed.'