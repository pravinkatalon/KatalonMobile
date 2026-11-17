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
import org.openqa.selenium.StaleElementReferenceException

def setTextSafely = { TestObject to, String text, int timeout = 60, int maxRetries = 3 ->
	int attempt = 0
	boolean success = false
	while (!success && attempt < maxRetries) {
		try {
			Mobile.setText(to, text, timeout)
			success = true
		} catch (StaleElementReferenceException e) {
			attempt++
			Mobile.delay(1)
		}
	}
	assert success : "Unable to set text '${text}' after ${maxRetries} retries due to repeated stale element references."
}

Mobile.startApplication('/Users/renae/work/katalon-mda/Apps/mda-staging.apk', true)

if (Mobile.verifyElementExist(findTestObject('Android/Authentication/btn_skip'), 5, FailureHandling.OPTIONAL)) {
	Mobile.tap(findTestObject('Android/Authentication/btn_skip'), 60)
}

Mobile.tap(findTestObject('Android/Authentication/btn_accept'), 60)
Mobile.tap(findTestObject('Android/Authentication/btn_ok'), 60)

Mobile.tap(findTestObject('Android/Authentication/create_account_btn'), 60)

Mobile.setText(findTestObject('Android/Authentication/create_account_first_name'), 'Tina', 60)

Mobile.setText(findTestObject('Android/Authentication/create_account_last_name'), 'Turner', 60)

Mobile.setText(findTestObject('Android/Authentication/create_account_email_field'), 'team1@gmail.com', 60)

Mobile.setText(findTestObject('Android/Authentication/create_account_msisdn_field'), '1876881539', 60)

Mobile.setText(findTestObject('Android/Authentication/create_account_password'), 'abc123!', 60)

Mobile.setText(findTestObject('Android/Authentication/create_account_password_confirm'), 'abc123!', 60)

Mobile.hideKeyboard()

Mobile.tap(findTestObject('Android/Authentication/create_account_submit_btn'), 60)

boolean msisdnLengthErrorShown = Mobile.verifyElementExist(findTestObject('Android/Authentication/error_msisdn_length'), 60, FailureHandling.OPTIONAL)
assert msisdnLengthErrorShown : 'Expected the "mobile number must have..." validation error to appear for an invalid MSISDN length.'

setTextSafely(findTestObject('Android/Authentication/create_account_msisdn_field'), '18768815395')

setTextSafely(findTestObject('Android/Authentication/create_account_email_field'), 'ifits!@gmail')

Mobile.hideKeyboard()

Mobile.tap(findTestObject('Android/Authentication/create_account_submit_btn'), 60)

boolean invalidFieldsErrorShown = Mobile.verifyElementExist(findTestObject('Android/Authentication/error_invalid_fields'), 60, FailureHandling.OPTIONAL)
assert invalidFieldsErrorShown : 'Expected the "could not create account, some required fields are invalid" error to appear for an invalid email format.'

setTextSafely(findTestObject('Android/Authentication/create_account_email_field'), 'team1@gmail.com')

setTextSafely(findTestObject('Android/Authentication/create_account_password'), 'abc123!')

setTextSafely(findTestObject('Android/Authentication/create_account_password_confirm'), '1234567gtrdes')

Mobile.hideKeyboard()

Mobile.tap(findTestObject('Android/Authentication/create_account_submit_btn'), 60)

boolean passwordsMismatchErrorShown = Mobile.verifyElementExist(findTestObject('Android/Authentication/error_passwords_mismatch'), 60, FailureHandling.OPTIONAL)
assert passwordsMismatchErrorShown : 'Expected the "passwords do not match" error to appear for mismatched password fields.'

Mobile.closeApplication()
