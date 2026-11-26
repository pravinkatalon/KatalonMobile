import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.llm.keyword.LlmKeywords as LLM
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.mobile.keyword.internal.MobileDriverFactory as MobileDriverFactory
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testng.keyword.TestNGBuiltinKeywords as TestNGKW
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.util.KeywordUtil as KeywordUtil
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import org.openqa.selenium.By as By
import org.openqa.selenium.WebElement as WebElement
import internal.GlobalVariable as GlobalVariable
import io.appium.java_client.AppiumDriver as AppiumDriver
import java.util.regex.Matcher as Matcher
import java.util.regex.Pattern as Pattern
import org.openqa.selenium.Keys as Keys

Mobile.startApplication('/Users/pravinr/Downloads/bookmyshow.apk', true)

CustomKeywords.'MobileAssist.setDeviceLocation'(13.0827, 80.2707)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Skip'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.EditText - Search for your city'), 60)

Mobile.setText(findTestObject('Android/BookMyShow/android.widget.EditText - Search for your city'), 'Chennai', 0)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Chennai'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Dismiss'), 0, FailureHandling.OPTIONAL)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Movies'), 60)

CustomKeywords.'MobileAssist.scrollToText'(findTestObject('Android/BookMyShow/androidx.recyclerview.widget.RecyclerView Movies Thumb (1)'), 
    Movie, 'UP', 10, false, 0.6)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Movie', [('movie') : Movie]), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - Movie Summary Info'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Book tickets'), 60)

date = CustomKeywords.'DateUtils.todayPlusDays'(2, 'dd')

//date = '26'
Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Date', [('date') : date]), 60)


CustomKeywords.'MobileAssist.scrollToText'(findTestObject('Android/BookMyShow/android.view.View Theaters List'), Theater, 
    'UP', 5, false, 0.4)


Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Theater', [('theater') : Theater]), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - Facility', [('facility') : Available_Facility]), 
    60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.ImageView Close Theater Details'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Before Time', [('time') : TimeSlot_Before]), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - How many seats'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.Button - Select Seats'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - After Time', [('time') : TimeSlot_After]), 60)

String ticketsText = Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - Total Tickets'), 30)

int numberOfTickets = (((ticketsText =~ '\\d+')[0]) as int)

println("Number of tickets: $numberOfTickets")

// 2. Seat object -> its parent -> all sibling seats
AppiumDriver driver = MobileDriverFactory.getDriver()

List<WebElement> seatElements = driver.findElements(By.xpath('//*[./android.view.View[contains(@content-desc, \'. Available\')]]' + 
        '/android.view.View[starts-with(@content-desc, \'Row \')]'))

println('Seat elements found: ' + seatElements.size())

// Status suffix is optional: Best Seller seats have none
Pattern seatPattern = Pattern.compile('Row\\s+(\\w+),\\s*Seat\\s+(\\d+),\\s*(.*?),\\s*Price\\s+(\\d+(?:\\.\\d+)?)(?:\\.\\s*(\\w+))?')

List<Map> seats = []

for (WebElement el : seatElements) {
    String desc = el.getAttribute('content-desc')

    Matcher seatMatch = seatPattern.matcher(desc == null ? '' : desc)

    if (seatMatch.find()) {
        String status = seatMatch.group(5) == null ? 'Best Seller' : seatMatch.group(5)

        seats.add([('row') : seatMatch.group(1), ('seat') : Integer.parseInt(seatMatch.group(2)), ('price') : new BigDecimal(
                seatMatch.group(4)), ('status') : status, ('available') : status != 'Booked', ('element') : el])
    }
}

println('Seats read: ' + seats.size())

if ((seatElements.size() > 0) && seats.isEmpty()) {
    println('First seat content-desc: ' + (seatElements[0]).getAttribute('content-desc'))
}

// 3. First series of numberOfTickets consecutive available seats (same row, seat numbers in sequence)
List<Map> series = null

Map<String, List> seatsByRow = new LinkedHashMap<String, List>()

for (Map s : seats) {
    String rowName = s.get('row')

    if (!(seatsByRow.containsKey(rowName))) {
        seatsByRow.put(rowName, new ArrayList<Map>())
    }
    
    seatsByRow.get(rowName).add(s)
}

for (List<Map> rowSeats : seatsByRow.values()) {
    // sort by seat number (insertion sort)
    List<Map> sorted = new ArrayList<Map>()

    for (Map s : rowSeats) {
        int seatNo = s.get('seat')

        int pos = 0

        while (pos < sorted.size()) {
            int otherNo = sorted.get(pos).get('seat')

            if (otherNo > seatNo) {
                break
            }
            
            pos++
        }
        
        sorted.add(pos, s)
    }
    
    for (int i = 0; (i + numberOfTickets) <= sorted.size(); i++) {
        boolean ok = true

        for (int j = 0; j < numberOfTickets; j++) {
            Map current = sorted.get(i + j)

            boolean isAvailable = current.get('available')

            int currentNo = current.get('seat')

            if (!(isAvailable)) {
                ok = false

                break
            }
            
            if (j > 0) {
                int previousNo = sorted.get((i + j) - 1).get('seat')

                if (currentNo != (previousNo + 1)) {
                    ok = false

                    break
                }
            }
        }
        
        if (ok) {
            series = sorted.subList(i, i + numberOfTickets)

            break
        }
    }
    
    if (series != null) {
        break
    }
}

if (series == null) {
    KeywordUtil.markFailed(('No ' + numberOfTickets) + ' consecutive available seats found')

    return null
}

println((((('Selected series: Row ' + series.get(0).get('row')) + ', Seats ') + series.get(0).get('seat')) + '-') + series.get(
        series.size() - 1).get('seat'))

// 4. Tap the first seat of the series
//def rect = series[0].element.getRect()

//Mobile.tapAtPosition(((rect.x + (rect.width / 2)) as int), ((rect.y + (rect.height / 2)) as int))

// 4. Tap the leftmost seat (on screen) of the series - the app fills to the right of it
WebElement firstSeat = null
int minX = Integer.MAX_VALUE
for (Map s : series) {
	WebElement el = s.get('element')
	int x = el.getRect().x
	if (x < minX) {
		minX = x
		firstSeat = el
	}
}
def rect = firstSeat.getRect()
Mobile.tapAtPosition(((rect.x + (rect.width / 2)) as int), ((rect.y + (rect.height / 2)) as int))
println('Tapped seat: ' + firstSeat.getAttribute('content-desc'))


// 5. Total cost of the selected seats
BigDecimal totalCost = ((series.sum({ 
            it.price
        })) as BigDecimal)

println('Seat prices: ' + series.collect({ 
            "$it.row$it.seat=$it.price"
        }).join(', '))

println("Total cost: $totalCost")

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.Button - Pay  Amount', [('totalCost') : totalCost]), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView Accessibility Information Accessibility features such as Audio Description'), 
    60)

if (Terms.equals('Accept')) {
    Mobile.tap(findTestObject('Android/BookMyShow/android.widget.Button - Accept'), 60)
} else {
    Mobile.tap(findTestObject('Android/BookMyShow/android.widget.Button - Okay'), 60)
}

if (Snack.equals('true')) {
	
	Mobile.delay(2)
	
	snackAvailable = Mobile.verifyElementVisible(findTestObject('Object Repository/Android/BookMyShow/android.widget.TextView - Grab a Bite'), 3)
	
    if (snackAvailable) {
		Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Add'), 60)

    Mobile.tap(findTestObject('Android/BookMyShow/android.widget.Button - Snacks Plus'), 60)

    Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Snacks Proceed'), 60)
	}
}



Mobile.setText(findTestObject('Android/BookMyShow/android.widget.EditText - email'), 'p@p.com', 0)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.EditText - Phone'), 60)

Mobile.setText(findTestObject('Android/BookMyShow/android.widget.EditText - Phone'), '6374630629', 0)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.Button - Update Details'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.ImageView Back From Billing'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - NO'), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - Number of Tickets', [('no') : numberOfTickets]), 
    60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - Movie', [('movie') : Movie]), 60)

Mobile.getText(findTestObject('Android/BookMyShow/android.widget.TextView - Billing Screen Date'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Continue'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - Pay by any UPI App'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.ImageView Back From UPI Details'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.ImageView Back From Payment'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.ImageView Back From Billing'), 60)

Mobile.tap(findTestObject('Android/BookMyShow/android.widget.TextView - YES'), 60)

Mobile.closeApplication()

