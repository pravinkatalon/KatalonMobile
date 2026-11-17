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

Mobile.startApplication('/Users/pravinr/Downloads/Shikhar.apk', true)

Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView -'), 60)

Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Next'), 60)

Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Next Heal'), 60)

Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Next Heal'), 60)

Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Login'), 60)

Mobile.setText(findTestObject('Android/Shikhar/android.widget.EditText - Enter Mobile Number'), '4533245665', 60)

Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Next (2)'), 60)

Mobile.tap(findTestObject('Android/Shikhar/android.widget.RadioButton - No'), 60)

Mobile.getText(findTestObject('Android/Shikhar/android.widget.TextView - Start new registration with HUL Distributor  Shikhar'), 
    60)

Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Next (3)'), 60)

Mobile.setText(findTestObject('Android/Shikhar/android.widget.EditText (4)'), '123456', 60)

Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Confirm'), 60)

Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Change Number'), 60)

Mobile.setText(findTestObject('Android/Shikhar/android.widget.EditText - Enter your mobile number'), '6374630621', 60)

Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Get verification code'), 60)

Mobile.setText(findTestObject('Android/Shikhar/android.widget.EditText (4)'), '144786', 60)

Mobile.tap(findTestObject('Android/Shikhar/android.widget.TextView - Confirm'), 60)

Mobile.closeApplication()

