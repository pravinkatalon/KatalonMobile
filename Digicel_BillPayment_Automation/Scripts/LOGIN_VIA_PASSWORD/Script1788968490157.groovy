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

Mobile.startApplication('/Users/renae/Downloads/app.apk', true)

Mobile.tap(findTestObject('accept_btn'), 60)

Mobile.tap(findTestObject('entrance_ok'), 60)

Mobile.tap(findTestObject('btn_LogIn'), 60)

Mobile.setText(findTestObject('android.widget.EditText - 1876 (1)'), '18762874267', 60)

Mobile.hideKeyboard()

Mobile.tap(findTestObject('login_continue'), 60)

Mobile.tap(findTestObject('android.widget.TextView - Use Password Instead'), 60)

Mobile.setText(findTestObject('android.widget.EditText (2)'), 'abc123', 60)

Mobile.hideKeyboard()

Mobile.tap(findTestObject('android.widget.Button - Log In'), 60)

Mobile.closeApplication()

