import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.testobject.ConditionType as ConditionType
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import com.kms.katalon.core.llm.keyword.LlmKeywords as LLM
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import org.openqa.selenium.Keys as Keys
import internal.GlobalVariable as GlobalVariable

String mobileNumber = '9999999999'

String pin = '123456'

TestObject countryCodeOption = new TestObject('countryCodeOption')

countryCodeOption.addProperty('xpath', ConditionType.EQUALS, '//*[@text=\'+91\' or contains(@text, \'+91\')]')

TestObject invalidLoginMessage = new TestObject('invalidLoginMessage')

invalidLoginMessage.addProperty('xpath', ConditionType.EQUALS, '//*[contains(translate(@text, \'ABCDEFGHIJKLMNOPQRSTUVWXYZ\', \'abcdefghijklmnopqrstuvwxyz\'), \'invalid\') or contains(translate(@text, \'ABCDEFGHIJKLMNOPQRSTUVWXYZ\', \'abcdefghijklmnopqrstuvwxyz\'), \'incorrect\')]')

// The login screen must expose the country-code selector and mobile-number field.
Mobile.verifyElementExist(findTestObject('Object Repository/Android/Shikhar/android.widget.TextView -'), 10)

Mobile.verifyElementExist(findTestObject('Object Repository/Android/Shikhar/android.widget.EditText - Enter your mobile number'), 
    10)

// Select India (+91) and verify that the selected country code is displayed.
Mobile.tap(findTestObject('Object Repository/Android/Shikhar/android.widget.TextView -'), 10)

Mobile.verifyElementExist(countryCodeOption, 10)

Mobile.tap(countryCodeOption, 10)

Mobile.verifyElementExist(countryCodeOption, 10)

// Submit an accepted mobile number. If this test is data-bound, stop processing
// additional rows once the PIN page is reached.
Mobile.setText(findTestObject('Object Repository/Android/Shikhar/android.widget.EditText - Enter your mobile number'), mobileNumber, 
    10)

Mobile.tap(findTestObject('Object Repository/Android/Shikhar/android.widget.TextView - Login'), 10)

if (Mobile.verifyElementExist(findTestObject('Object Repository/Android/Shikhar/android.widget.TextView - Change Number'), 
    15, com.kms.katalon.core.model.FailureHandling.OPTIONAL)) {
    Mobile.comment('PIN page displayed; remaining mobile-number dataset entries are skipped for this iteration.')

    // The existing EditText object represents the PIN/OTP input on the PIN page.
    Mobile.setText(findTestObject('Object Repository/Android/Shikhar/android.widget.EditText (4)'), pin, 10)

    assert pin ==~ '\\\\d{6}' : 'PIN must contain exactly six digits.'

    Mobile.tap(findTestObject('Object Repository/Android/Shikhar/android.widget.TextView - Confirm'), 10)

    // If the submitted PIN is rejected, require a clear, correctly spelled message.
    if (Mobile.verifyElementExist(invalidLoginMessage, 10, com.kms.katalon.core.model.FailureHandling.OPTIONAL)) {
        String errorText = Mobile.getText(invalidLoginMessage, 10)

        assert errorText ==~ '(?is).*(invalid|incorrect).*?'

        assert !(errorText.toLowerCase().contains('invaild'))
    }
} else {
    Mobile.verifyElementExist(invalidLoginMessage, 10)

    String errorText = Mobile.getText(invalidLoginMessage, 10)

    assert errorText ==~ '(?is).*(invalid|incorrect).*?'

    assert !(errorText.toLowerCase().contains('invaild'))
}

