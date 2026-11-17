import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling

import internal.GlobalVariable as GlobalVariable

Boolean selectedUseOtp = useOtp == null ? GlobalVariable.G_UseOTP : useOtp
String selectedMsisdn = msisdn?.toString()?.trim()
String selectedPassword = password?.toString()

if (!selectedMsisdn) {
	selectedMsisdn = GlobalVariable.G_MSISDN?.toString()?.trim()
}
if (selectedPassword == null || selectedPassword.isEmpty()) {
	selectedPassword = GlobalVariable.G_Password?.toString()
}

assert selectedUseOtp != null : 'Login mode must be configured as true for OTP login or false for password login.'

String loginTestCase = selectedUseOtp ?
	'Test Cases/Android/Authentication/TC_LOGIN_WITH_OTP' :
	'Test Cases/Android/Authentication/TC_LOGIN_WITH_PASSWORD'

Mobile.callTestCase(
	findTestCase(loginTestCase),
	[
		('msisdn') : selectedMsisdn,
		('password') : selectedPassword
	],
	FailureHandling.STOP_ON_FAILURE)
