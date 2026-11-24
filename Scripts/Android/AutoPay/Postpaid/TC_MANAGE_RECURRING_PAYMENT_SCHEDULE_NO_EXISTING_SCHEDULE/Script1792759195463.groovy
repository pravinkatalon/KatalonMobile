import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling

Mobile.callTestCase(findTestCase('Android/Authentication/TC_LOGIN'), [:], FailureHandling.STOP_ON_FAILURE)

Mobile.tap(findTestObject('Android/Bill Payment/bills_tab'), 60)

Mobile.tap(findTestObject('Android/Bill Payment/recurring_payments_button'), 60)

boolean noSchedulesShown = Mobile.verifyElementExist(
	findTestObject('Android/Bill Payment/no_schedules_message'),
	30,
	FailureHandling.OPTIONAL)
assert noSchedulesShown : 'No schedules message was not displayed.'