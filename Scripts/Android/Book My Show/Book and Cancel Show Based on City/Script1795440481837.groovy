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

Mobile.startApplication('/Users/pravinr/Katalon Studio/KatalonMobile/Resources/Apps/bookmyshow.apk', true)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Skip'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.EditText - Search for your city'), 60)

Mobile.setText(findTestObject('Android/BookMyShow/android.widget.EditText - Search for your city'), 'Chennai', 0)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Chennai'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Dismiss'), 0, FailureHandling.OPTIONAL)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Movies'), 60)

CustomKeywords.'MobileAssist.scrollToText'(findTestObject('Android/BookMyShow/androidx.recyclerview.widget.RecyclerView Movies Thumb (1)'), 
    'Sathyavan', 'UP', 5, false)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Sathyavan Savithiri'), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - Movie Summary Info'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Book tickets'), 60)

CustomKeywords.'MobileAssist.scrollToText'(findTestObject('Android/BookMyShow/android.view.View Theaters List'), 'MAYAJAAL', 
    'UP', 5, false)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - 26'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - MAYAJAAL Multiplex ECR, Chennai'), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - Parking Facility'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.ImageView (1)'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - 0700 PM'), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - How many seats'), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - FIRST CLASS'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - 0330 PM'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.Button - Select Seats'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.Button - Pay  Amount'), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView Accessibility Information Accessibility features such as Audio Description'), 
    60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.Button - Okay'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Add'), 60)

Mobile.tap(findTestObject('android.widget.Button (1)'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Proceed'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.EditText - egabc at gmail com (1)'), 60)

Mobile.setText(findTestObject('Android/BookMyShow/android.widget.EditText - egabc at gmail com (1)'), 'p@p.com', 0)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.EditText - eg91480XXXXX'), 60)

Mobile.setText(findTestObject('Android/BookMyShow/android.widget.EditText - eg91480XXXXX'), '8754450054', 0)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.Button - Update Details'), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - 2'), 60)

Mobile.getText(findTestObject('android.widget.TextView - Sathyavan Savithiri (1)'), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - Thu, 26 Nov, 2026  0330 PM'), 60)

Mobile.clearText(findTestObject('Android/BookMyShow/android.widget.EditText - pravin at l com'), 60)

Mobile.setText(findTestObject('Android/BookMyShow/android.widget.EditText - egabc at gmail com'), 'pravinrajpaul@gmail.com', 
    60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.ImageView'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Continue'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Pay by any UPI App'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.ImageView (1)'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.ImageView (2)'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.ImageView (3)'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - YES'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.ImageView (4)'), 60)

Mobile.closeApplication()

