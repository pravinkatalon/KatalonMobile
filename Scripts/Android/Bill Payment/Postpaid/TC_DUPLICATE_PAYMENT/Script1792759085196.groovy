import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.util.KeywordUtil as KeywordUtil
import java.util.concurrent.ThreadLocalRandom as ThreadLocalRandom
import com.kms.katalon.core.testobject.TestObject

String duplicateAmount = BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(100, 1100), 2).toPlainString()

KeywordUtil.logInfo("Duplicate payment amount: $duplicateAmount")

Mobile.callTestCase(findTestCase('Test Cases/Android/Bill Payment/Postpaid/TC_PAY_ENTERED_AMOUNT_SAVED_CARD'), [('paymentAmount') : duplicateAmount], 
    FailureHandling.STOP_ON_FAILURE)

Mobile.tap(findTestObject('Android/Bill Payment/btn_back_to_my_digicel'), 60)

Mobile.tap(findTestObject('Android/Bill Payment/btn_pay_bill'), 60)

Mobile.tap(findTestObject('Android/Bill Payment/radio_pay_in_full'), 60)

Mobile.setText(findTestObject('Android/Bill Payment/input_payment_amount'), duplicateAmount, 60)

Mobile.hideKeyboard()

//if (Mobile.verifyElementExist(findTestObject('Android/Bill Payment/toggle_recurring_payment'), 5, FailureHandling.OPTIONAL)) {
//	Mobile.tap(findTestObject('Android/Bill Payment/toggle_recurring_payment'), 60)
//}
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

Mobile.setText(findTestObject('Android/Bill Payment/input_saved_card_cvv'), '123', 60)

Mobile.hideKeyboard()

Mobile.tap(findTestObject('Android/Bill Payment/cvc_pay'), 60)

boolean duplicateShown = Mobile.verifyElementExist(findTestObject('Android/Bill Payment/txt_duplicate_payment_error'), 30, 
    FailureHandling.OPTIONAL)

assert duplicateShown : 'Duplicate payment warning did not appear on the second attempt.'

