import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import internal.GlobalVariable as GlobalVariable
import java.util.concurrent.ThreadLocalRandom as ThreadLocalRandom
import com.kms.katalon.core.testobject.TestObject

String enteredAmount = paymentAmount.toString().trim()

if (!(enteredAmount)) {
	enteredAmount = BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(100, 1100), 2).toPlainString()
}

Mobile.callTestCase(findTestCase('Test Cases/Android/Authentication/TC_LOGIN'), [:], FailureHandling.STOP_ON_FAILURE)

Mobile.tap(findTestObject('Android/Bill Payment/tab_home_entertainment'), 60)

String accountName = GlobalVariable.G_HomeAccount.toString().trim()

assert accountName : 'G_HomeAccount must be configured in the selected execution profile.'

int maxSwipes = 5
int swipeCount = 0
while (!Mobile.verifyElementExist(findTestObject('Android/Bill Payment/home_account_tap', [('accountName') : accountName]), 2, FailureHandling.OPTIONAL) && swipeCount < maxSwipes) {
	Mobile.swipe(360, 1300, 360, 600)
	swipeCount++
}
Mobile.tap(findTestObject('Android/Bill Payment/home_account_tap', [('accountName') : accountName]), 60)

Mobile.tap(findTestObject('Android/Bill Payment/btn_pay_bill'), 60)
Mobile.tap(findTestObject('Android/Bill Payment/radio_pay_in_full'), 60)
Mobile.setText(findTestObject('Android/Bill Payment/input_payment_amount'), enteredAmount, 60)
Mobile.hideKeyboard()
TestObject toggle = findTestObject('Android/Bill Payment/toggle_recurring_payment')
if (Mobile.verifyElementExist(toggle, 5, FailureHandling.OPTIONAL)) {
	String isOn = Mobile.getAttribute(toggle, 'checked', 10)
	if (isOn != 'true') {
		Mobile.tap(toggle, 60)
	}
}
Mobile.tap(findTestObject('Android/Bill Payment/btn_yes_continue'), 60)
Mobile.tap(findTestObject('Android/Bill Payment/autopay_agree_button'), 60)
Mobile.tap(findTestObject('Android/Bill Payment/card_saved_payment_method'), 60)
Mobile.tap(findTestObject('Android/Bill Payment/payment_method_okay'), 60)
Mobile.setText(findTestObject('Android/Bill Payment/input_saved_card_cvv'), '123', 60)
Mobile.hideKeyboard()
Mobile.tap(findTestObject('Android/Bill Payment/cvc_pay'), 60)

boolean successShown = Mobile.verifyElementExist(findTestObject('Android/Bill Payment/txt_payment_success'), 30, FailureHandling.OPTIONAL)
assert successShown : 'Payment success screen did not appear within 30 seconds.'

TestObject savedMessage = findTestObject('Android/Bill Payment/autopay_payment_method_saved_message')
int msgSwipes = 0
while (!Mobile.verifyElementExist(savedMessage, 2, FailureHandling.OPTIONAL) && msgSwipes < maxSwipes) {
	Mobile.swipe(360, 1300, 360, 600)
	msgSwipes++
}
boolean savedMessageShown = Mobile.verifyElementExist(savedMessage, 5, FailureHandling.OPTIONAL)
assert savedMessageShown : 'Autopay payment method saved message was not displayed.'

TestObject viewRecurringBtn = findTestObject('Android/Bill Payment/view_recurring_payments_button')
int btnSwipes = 0
while (!Mobile.verifyElementExist(viewRecurringBtn, 2, FailureHandling.OPTIONAL) && btnSwipes < maxSwipes) {
	Mobile.swipe(360, 1300, 360, 600)
	btnSwipes++
}
boolean viewRecurringShown = Mobile.verifyElementExist(viewRecurringBtn, 5, FailureHandling.OPTIONAL)
assert viewRecurringShown : 'View recurring payments button was not displayed.'