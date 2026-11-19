import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.llm.keyword.LlmKeywords as LLM
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

'Open the Shikhar application on the device.'
Mobile.startApplication('Resources/Apps/Shikhar.apk', true)

'Accept the first introductory app prompt.'
Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView -'), 60)

'Accept the second introductory app prompt.'
Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Next'), 60)

'Accept the third introductory app prompt.'
Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Next (1)'), 60)

'Accept the final introductory prompt and continue to the login screen.'
Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Next (1)'), 60)

'Open the login flow.'
Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Login'), 60)

'Enter an invalid mobile number to test invalid-number handling.'
Mobile.setText(findTestObject('Android/Shikhar/android.widget.EditText - Enter Mobile Number'), '4533245665', 60)

'Continue to mobile-number validation.'
Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Next (2)'), 60)

'Select No to start a new registration with the HUL Distributor Shikhar option.'
Mobile.tap(findTestObject('Android/Shikhar/android.widget.RadioButton - No'), 60)

'Retrieve the text displayed for the invalid mobile-number flow.'
Mobile.getText(findTestObject('Android/Shikhar/android.widget.TextView - Start new registration with HUL Distributor  Shikhar'), 60)

'Force a failure to verify the negative test behavior.'
assert 1 == 2

'Continue to PIN verification.'
Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Next (3)'), 60)

'Enter the PIN to validate the invalid mobile number.'
Mobile.setText(findTestObject('Android/Shikhar/android.widget.EditText (4)'), '123456', 60)

'Confirm the PIN and validate the invalid-mobile response.'
Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Confirm'), 60)

'Change the mobile number for the valid-login verification.'
Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Change Number'), 60)

'Enter the valid mobile number.'
Mobile.setText(findTestObject('Android/Shikhar/android.widget.EditText - Enter your mobile number'), '6374630621', 60)

'Request the verification code for the valid mobile number.'
Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Get verification code'), 60)

'Enter the verification PIN to check login.'
Mobile.setText(findTestObject('Android/Shikhar/android.widget.EditText (4)'), '144786', 60)

'Confirm the verification PIN and complete login.'
Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Confirm'), 60)

'Close the Shikhar application.'
Mobile.closeApplication()
