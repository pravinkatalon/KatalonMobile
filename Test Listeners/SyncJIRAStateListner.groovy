import com.kms.katalon.core.annotation.AfterTestCase
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.context.TestCaseContext
import com.kms.katalon.util.CryptoUtil
import internal.GlobalVariable

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList

class SyncJIRAStateListner {
	// =========================================================================
	// USER CONFIGURATION
	// =========================================================================
	// -------------------------------------------------------------------------
	// TRANSITION MAP:  current Jira status  ->  Katalon result  ->  transition ID(s) to apply, in order
	//
	// Jira workflows only allow certain moves from each status (e.g. Done cannot go straight to
	// In Progress, it must go through Reopen first). So the listener reads the issue's CURRENT
	// status and picks the transitions configured for that status + the Katalon result.
	//
	//   - Status keys   : Jira status NAME (case-insensitive) or status ID, e.g. "Done" or "10002"
	//   - "*"           : fallback for any status not listed
	//   - Result keys   : Katalon test case status (see list below). A result not listed = no update.
	//   - Value         : list of transition IDs applied one after another, e.g. ["71", "21"]
	//                     = Reopen, then Start Progress. [] = already in the right status, no update.
	//
	// Katalon test case statuses (testCaseContext.getTestCaseStatus()):
	//   PASSED     - completed, all verifications passed
	//   FAILED     - a verification / assertion failed
	//   ERROR      - script error / unexpected exception
	//   WARNING    - completed, but a step logged a warning
	//   SKIPPED    - skipped (e.g. skipThisTestCase())
	//   INCOMPLETE - execution stopped / interrupted
	//   NOT_RUN    - never executed
	//
	// If no mapping is found, the listener logs the issue's current status and its valid transitions
	// so you can add the missing entry.
	// -------------------------------------------------------------------------
	private static final Map<String, Map<String, List<String>>> TRANSITION_MAP = [
		// current status : [ Katalon result : [transition IDs] ]
		"To Do"       : [ PASSED: ["31"],  FAILED: ["21"],  ERROR: ["21"] ],   // To Do -> Done / In Progress
		"In Progress" : [ PASSED: ["31"],  FAILED: ["21"],      ERROR: ["21"]     ],   // already In Progress on failure
		"Done"        : [ PASSED: ["31"],      FAILED: ["21"],  ERROR: ["21"] ],   // already Done on pass
		// Example for a workflow where Done can only be reopened first (Reopen = 71, then In Progress = 21):
		// "Done"     : [ PASSED: [],      FAILED: ["71", "21"], ERROR: ["71", "21"] ],
		"*"           : [ PASSED: ["31"],  FAILED: ["21"],  ERROR: ["21"] ]    // any other status
	]

	// -------------------------------------------------------------------------
	// Resolution per Katalon status - only used when the transition's screen has a Resolution
	// field (e.g. moving to Closed/Done). Must match a resolution name in your Jira (case-insensitive).
	// Leave "" to not set one. If Jira requires it and none matches, the allowed values are logged.
	// -------------------------------------------------------------------------
	private static final String RESOLUTION_PASSED     = "Done"
	private static final String RESOLUTION_FAILED     = ""
	private static final String RESOLUTION_ERROR      = ""
	private static final String RESOLUTION_WARNING    = ""
	private static final String RESOLUTION_SKIPPED    = ""
	private static final String RESOLUTION_INCOMPLETE = ""
	private static final String RESOLUTION_NOT_RUN    = ""

	private static final Map<String, String> RESOLUTIONS = [
		PASSED    : RESOLUTION_PASSED,
		FAILED    : RESOLUTION_FAILED,
		ERROR     : RESOLUTION_ERROR,
		WARNING   : RESOLUTION_WARNING,
		SKIPPED   : RESOLUTION_SKIPPED,
		INCOMPLETE: RESOLUTION_INCOMPLETE,
		NOT_RUN   : RESOLUTION_NOT_RUN
	]

	// -------------------------------------------------------------------------
	// HOW TO FIND JIRA TRANSITION IDs FOR AN ISSUE TYPE
	// Transition IDs belong to the workflow used by the issue type (they differ between workflows/projects).
	//
	// 1. API - take any issue of that issue type and call:
	//      Jira Cloud : curl -u "<email>:<api-token>" -H "Accept: application/json" \
	//                     "https://<site>.atlassian.net/rest/api/2/issue/<ISSUE-KEY>/transitions"
	//      Jira DC    : curl -H "Authorization: Bearer <PAT>" -H "Accept: application/json" \
	//                     "https://<jira-host>/rest/api/2/issue/<ISSUE-KEY>/transitions"
	//    (or open that URL in a browser while logged into Jira)
	//    Response: {"transitions":[{"id":"31","name":"Done","to":{"name":"Done"}}, ...]}
	//    -> use "id". NOTE: only transitions available from the issue's CURRENT status are listed;
	//       check issues in different statuses if your workflow restricts transitions.
	//
	// 2. Full workflow (all transitions, any status):
	//      Jira UI    : Project settings > Workflows > edit the workflow for the issue type > "Text" view
	//                   (lists every transition with its ID)
	//      Jira Cloud : GET /rest/api/3/workflowscheme/project?projectId=<projectId>   -> workflow per issue type
	//                   GET /rest/api/3/workflow/search?workflowName=<name>&expand=transitions
	//
	// 3. At runtime: if a configured ID is invalid, Jira returns HTTP 400 and this listener
	//    logs the valid transitions for that issue.
	// -------------------------------------------------------------------------


	// Jira connection - lookup order for each value:
	//   1. Values below (leave "" to skip)
	//   2. GlobalVariable (Execution Profile) with the names further down
	//   3. Jira plugin settings file (settings/external/...jira-plugin.properties)
	// If any value is missing from all three, the status update is skipped.
	private static final String CONFIG_BASE_URL  = ""   // e.g. https://yourcompany.atlassian.net
	private static final String CONFIG_USERNAME  = ""   // Jira account email
	private static final String CONFIG_API_TOKEN = ""   // plain Atlassian API token
	private static final List<String> CONFIG_ISSUE_TYPES = []  // allowed Jira issue types (names or IDs), e.g. ["Test", "Test Case"]

	// NAME of the test case variable that holds the Jira ID(s), e.g. "jiraId".
	// Lookup: this value -> GlobalVariable JIRA_KEY_VARIABLE. If neither is set, only the plugin link is used.
	// The variable itself can be a String ("ORHR-776", "A-1,A-2", "A-1;A-2", "A-1, A-2; A-3") or a List.
	private static final String CONFIG_JIRA_KEY_VARIABLE = ""

	// NAMES of the GlobalVariables to look up (not values). Put the values in your Execution Profile.
	private static final String GV_BASE_URL  = "JIRA_BASE_URL"
	private static final String GV_USERNAME  = "JIRA_USERNAME"
	private static final String GV_API_TOKEN = "JIRA_API_TOKEN"
	private static final String GV_ISSUE_TYPES = "JIRA_ISSUE_TYPES"
	private static final String GV_JIRA_KEY_VARIABLE = "JIRA_KEY_VARIABLE"
	// =========================================================================

	@AfterTestCase
	def syncJiraStatusToJira(TestCaseContext testCaseContext) {
		try {
			doSync(testCaseContext)
		} catch (Throwable t) {
			println "[JIRA SYNC] ERROR: Unexpected " + t.getClass().getName() + ": " + t.getMessage()
			t.getStackTrace().take(5).each { println "[JIRA SYNC]     at " + it }
		}
	}

	private void doSync(TestCaseContext testCaseContext) {
		String testCaseId = testCaseContext.getTestCaseId()
		String executionStatus = testCaseContext.getTestCaseStatus()

		println "[JIRA SYNC] --- STARTING SYNC ---"
		println "[JIRA SYNC] Test Case ID: " + testCaseId
		println "[JIRA SYNC] Execution Status: " + executionStatus

		String result = executionStatus?.toUpperCase()
		if (!TRANSITION_MAP.values().any { it.containsKey(result) }) {
			println "[JIRA SYNC] Execution status '" + executionStatus + "' has no transitions configured in TRANSITION_MAP - no update."
			return
		}

		Set<String> jiraKeys = getJiraKeys(testCaseContext)
		if (!jiraKeys) {
			println "[JIRA SYNC] SKIPPED: no Jira ID (plugin link or test case variable) - listener not applicable."
			return
		}
		println "[JIRA SYNC] Resolved Jira Keys: " + jiraKeys

		// Each value: class config -> GlobalVariable -> Jira plugin settings file.
		// If any one of them is missing from all three, skip the status update.
		Properties props = getJiraProperties()   // null on pipeline / KRE runs without settings
		println "[JIRA SYNC] Jira plugin settings file: " + (props ? "found" : "not found")

		Map baseUrl  = resolve(CONFIG_BASE_URL,  GV_BASE_URL,  props, "auth.serverUrl", false)
		Map username = resolve(CONFIG_USERNAME,  GV_USERNAME,  props, "auth.username",  false)
		Map secret   = resolve(CONFIG_API_TOKEN, GV_API_TOKEN, props, "auth.password",  true)
		Map issueTypes = resolveIssueTypes(props)

		String url = baseUrl.value.replaceAll("/+\$", "")
		println "[JIRA SYNC] Jira Base URL: " + (url ?: "<MISSING>") + " (" + baseUrl.source + ")"
		println "[JIRA SYNC] Username: " + (username.value ?: "<NONE - using Bearer PAT>") + " (" + username.source + ")"
		println "[JIRA SYNC] Secret loaded: " + (secret.value ? "yes" : "NO") + " (" + secret.source + ")"
		println "[JIRA SYNC] Allowed Issue Types: " + (issueTypes.value ?: "<MISSING>") + " (" + issueTypes.source + ")"

		List<String> missing = []
		if (!url) missing << GV_BASE_URL
		// Username is optional: empty = Jira DC Personal Access Token (Bearer)
		if (!secret.value) missing << GV_API_TOKEN
		if (!issueTypes.value) missing << GV_ISSUE_TYPES
		if (missing) {
			println "[JIRA SYNC] SKIPPED: " + missing + " not found in class config, GlobalVariables or Jira settings - status not updated."
			return
		}

		// Jira Cloud:              username = email,  token = API token          -> Basic
		// Jira DC (PAT):           username empty,    token = Personal Access Token -> Bearer
		// Jira DC (user/password): username = login,  token = password          -> Basic
		String authHeader
		if (username.value) {
			authHeader = "Basic " + Base64.getEncoder().encodeToString((username.value + ":" + secret.value).getBytes("UTF-8"))
			println "[JIRA SYNC] Auth: Basic as '" + username.value + "'"
		} else {
			authHeader = "Bearer " + secret.value
			println "[JIRA SYNC] Auth: Bearer (Personal Access Token)"
		}

		// Login check first. If it fails, stop here: every further call would be another failed
		// login and can push the account into CAPTCHA lockout (AUTHENTICATION_DENIED).
		if (!checkLogin(url, authHeader)) {
			println "[JIRA SYNC] SKIPPED: login to Jira failed - no further calls made to protect the account. Status not updated."
			println "[JIRA SYNC] --- END SYNC ---"
			return
		}

		String resolution = RESOLUTIONS[result]?.trim()
		if (resolution) println "[JIRA SYNC] Resolution for " + result + ": " + resolution

		// Each key is handled on its own - one failure does not stop the others
		Map<String, String> results = [:]
		for (String jiraKey : jiraKeys) {
			Map issueType = getIssue(url, authHeader, jiraKey)
			if (!issueType) {
				results[jiraKey] = "skipped (could not read issue type)"
				continue
			}
			boolean allowed = issueTypes.value.any { it.equalsIgnoreCase(issueType.name) || it == issueType.id }
			println "[JIRA SYNC] Issue Type of " + jiraKey + ": " + issueType.name + " (id " + issueType.id + ") - allowed=" + allowed
			if (!allowed) {
				println "[JIRA SYNC] Issue type mismatch for " + jiraKey + ": Jira has '" + issueType.name + "' (id " + issueType.id +
						"), allowed list " + issueTypes.value + " - names compared ignoring case, IDs exactly"
				results[jiraKey] = "skipped (issue type '" + issueType.name + "' not in " + issueTypes.value + ")"
				continue
			}

			// Pick the transitions for the issue's CURRENT status + this Katalon result
			Map entry = findStatusEntry(issueType.statusName, issueType.statusId)
			List<String> steps = entry?.map?.get(result)
			if (steps == null) {
				println "[JIRA SYNC] No mapping for current status '" + issueType.statusName + "' (id " + issueType.statusId + ") + result " + result +
						" in TRANSITION_MAP - not updated."
				logValidTransitions(url, authHeader, jiraKey)
				results[jiraKey] = "skipped (no mapping for status '" + issueType.statusName + "' + " + result + ")"
				continue
			}
			println "[JIRA SYNC] " + jiraKey + ": current status '" + issueType.statusName + "' + " + result +
					" -> transitions " + steps + " (matched '" + entry.key + "')"
			if (steps.isEmpty()) {
				println "[JIRA SYNC] " + jiraKey + " is already in the right status for " + result + " - nothing to do."
				results[jiraKey] = "no change (already '" + issueType.statusName + "')"
				continue
			}

			String outcome = "updated"
			for (int i = 0; i < steps.size(); i++) {
				println "[JIRA SYNC] " + jiraKey + ": step " + (i + 1) + "/" + steps.size() + " - transition " + steps[i]
				String r = transitionIssue(url, authHeader, jiraKey, steps[i].toString().trim(), resolution)
				if (r != "updated") {
					outcome = r + " at step " + (i + 1) + "/" + steps.size() + (i > 0 ? " (earlier steps were applied)" : "")
					break
				}
			}
			results[jiraKey] = outcome
		}

		int updated = results.count { it.value == "updated" }
		println "[JIRA SYNC] Summary: updated " + updated + "/" + results.size() + " - " + results.collect { it.key + ": " + it.value }.join(", ")
		println "[JIRA SYNC] --- END SYNC ---"
	}

	// ---- Settings -----------------------------------------------------------

	// Returns [value: ..., source: "class config" | "GlobalVariable.X" | "settings" | "not found"]
	private Map resolve(String classValue, String gvName, Properties props, String settingsKey, boolean encrypted) {
		if (classValue?.trim()) return [value: classValue.trim(), source: "class config"]

		String gv = readGlobalVariable(gvName)
		if (gv) return [value: gv, source: "GlobalVariable." + gvName]

		if (props) {
			String raw = props.getProperty(settingsKey)
			String v = encrypted ? decryptSecret(raw, props) : clean(raw)
			if (v) return [value: v, source: "settings"]
		}
		return [value: "", source: "not found"]
	}

	// Issue types: class config -> GlobalVariable (comma-separated) -> settings file (submit.jiraIssueType)
	// Returns [value: List<String> of names/IDs, source: ...]
	private Map resolveIssueTypes(Properties props) {
		List<String> fromClass = toList(CONFIG_ISSUE_TYPES)
		if (fromClass) return [value: fromClass, source: "class config"]

		// GlobalVariable can be a List type in the profile (["Test", "Test Case"]) or a comma-separated String
		List<String> fromGv = toList(readGlobalVariableRaw(GV_ISSUE_TYPES))
		if (fromGv) return [value: fromGv, source: "GlobalVariable." + GV_ISSUE_TYPES]

		if (props) {
			try {
				// Stored as quoted JSON with escaped quotes: "{\"defaultURI\":...}"
				String raw = clean(props.getProperty("submit.jiraIssueType")).replace('\\"', '"')
				if (raw) {
					def json = new JsonSlurper().parseText(raw)
					String id = (json.defaultURI =~ /issuetype\/(\d+)/).with { it.find() ? it.group(1) : null }
					if (id) {
						// Match by ID - the name alone is ambiguous (several "Test" types can exist)
						String name = json.jiraObjects?.find { it.id?.toString() == id }?.name
						println "[JIRA SYNC] Settings default issue type: " + name + " (id " + id + ")"
						return [value: [id], source: "settings"]
					}
				}
			} catch (Exception e) {
				println "[JIRA SYNC] WARNING: Could not read issue type from settings: " + e.getMessage()
			}
		}
		return [value: [], source: "not found"]
	}

	// Accepts a List, or a comma-separated String. Spaces inside names are kept ("Test Case").
	private List<String> toList(def v) {
		if (v == null) return []
		Collection items = (v instanceof Collection) ? v : v.toString().split(",").toList()
		return items.findAll { it != null }.collect { it.toString().trim() }.findAll { it }
	}

	private def readGlobalVariableRaw(String name) {
		try {
			return GlobalVariable."$name"
		} catch (MissingPropertyException ignored) {
			return null
		}
	}

	// Reads issue type and CURRENT status in one call
	private Map getIssue(String baseUrl, String authHeader, String jiraKey) {
		try {
			Map resp = http("GET", baseUrl + "/rest/api/2/issue/" + jiraKey + "?fields=issuetype,status", authHeader, null)
			if (resp.code == 200) {
				def f = new JsonSlurper().parseText(resp.body).fields
				def t = f?.issuetype
				def st = f?.status
				println "[JIRA SYNC] Actual issue type in Jira for " + jiraKey + ": name='" + t?.name + "' id=" + t?.id + " subtask=" + t?.subtask
				println "[JIRA SYNC] Current status of " + jiraKey + ": '" + st?.name + "' (id " + st?.id + ", category " + st?.statusCategory?.name + ")"
				return [name: t?.name?.toString(), id: t?.id?.toString(), statusName: st?.name?.toString(), statusId: st?.id?.toString()]
			}
			println "[JIRA SYNC] FAILURE: issue lookup for " + jiraKey + " - HTTP " + resp.code + " - issue type and status UNKNOWN"
		} catch (Exception e) {
			println "[JIRA SYNC] ERROR: issue lookup failed: " + e.getClass().getName() + ": " + e.getMessage()
			println "[JIRA SYNC] Diagnosis: " + diagnoseException(e)
		}
		return null
	}

	// Status name (case-insensitive) or ID match, then "*" fallback. Returns [key: matchedKey, map: resultMap] or null.
	private Map findStatusEntry(String statusName, String statusId) {
		def e = TRANSITION_MAP.find { k, v -> k != "*" && (k.equalsIgnoreCase(statusName ?: "") || k == statusId) }
		if (!e && TRANSITION_MAP.containsKey("*")) e = TRANSITION_MAP.find { k, v -> k == "*" }
		return e ? [key: e.key, map: e.value] : null
	}

	private void logValidTransitions(String baseUrl, String authHeader, String jiraKey) {
		try {
			Map all = http("GET", baseUrl + "/rest/api/2/issue/" + jiraKey + "/transitions", authHeader, null)
			if (all.code == 200) {
				def list = new JsonSlurper().parseText(all.body).transitions.collect { it.id + "=" + it.name + " -> '" + it.to?.name + "'" }
				println "[JIRA SYNC] Valid transitions for " + jiraKey + " right now: " + list
			}
		} catch (Exception ignored) { }
	}

	private String readGlobalVariable(String name) {
		try {
			def v = GlobalVariable."$name"
			return v != null ? v.toString().trim() : ""
		} catch (MissingPropertyException ignored) {
			return ""   // variable not defined in the execution profile
		}
	}

	private Properties getJiraProperties() {
		File file = new File(RunConfiguration.getProjectDir() + "/settings/external/com.katalon.katalon-studio-jira-plugin.properties")
		println "[JIRA SYNC] Settings file: " + file.getAbsolutePath() + " (exists=" + file.exists() + ")"
		if (!file.exists()) return null
		Properties props = new Properties()
		file.withInputStream { props.load(it) }
		return props
	}

	// Plugin stores values wrapped in literal quotes: "value"
	private String clean(String raw) {
		if (!raw) return ""
		String v = raw.trim()
		if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
			v = v.substring(1, v.length() - 1)
		}
		return v.trim()
	}

	// The plugin stores the password as a JSON string (Gson): wrapped in quotes, with special characters
	// escaped (backslash doubled, = < > & ' as unicode escapes). It may be encrypted or plain text.
	// The value must be JSON-decoded exactly like the plugin does, otherwise Jira rejects the password.
	private String decryptSecret(String raw, Properties props) {
		String stored = raw?.trim()
		if (!stored) return ""
		String plain
		if ("false".equalsIgnoreCase(props?.getProperty("auth.encryptionEnabled")?.trim())) {
			plain = stored
		} else {
			try {
				plain = CryptoUtil.decode(CryptoUtil.getDefault(clean(stored)))
			} catch (Exception e) {
				// Not valid encrypted data - the plugin saved it as plain text
				println "[JIRA SYNC] Settings password is not encrypted - using it as plain text."
				plain = stored
			}
		}
		return decodeJsonString(plain)
	}

	private String decodeJsonString(String v) {
		String t = v?.trim() ?: ""
		if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
			try {
				return (String) new JsonSlurper().parseText("[" + t + "]")[0]
			} catch (Exception e) {
				println "[JIRA SYNC] WARNING: could not decode settings password (" + e.getClass().getSimpleName() + ") - only stripping quotes."
				return clean(t)
			}
		}
		return t
	}

	// ---- Jira keys ----------------------------------------------------------

	// Keys = plugin link in .tc  +  configured test case variable (duplicates removed)
	private static final String JIRA_KEY_PATTERN = "^[A-Z][A-Z0-9_]+-\\d+\$"

	private Set<String> getJiraKeys(TestCaseContext ctx) {
		Set<String> keys = new LinkedHashSet<>()

		String linked = keyFromTcFile(ctx.getTestCaseId())
		if (linked) {
			println "[JIRA SYNC] Jira key from plugin link: " + linked
			keys << linked.toUpperCase()
		}

		String varName = CONFIG_JIRA_KEY_VARIABLE?.trim() ?: readGlobalVariable(GV_JIRA_KEY_VARIABLE)
		if (!varName) {
			println "[JIRA SYNC] Jira key variable name not configured - using plugin link only."
			return keys
		}

		def raw = ctx.getTestCaseVariables()?.get(varName)
		List<String> fromVar = parseKeys(raw)
		println "[JIRA SYNC] Jira keys from variable '" + varName + "': " + (fromVar ?: "<none>")
		keys.addAll(fromVar)
		return keys
	}

	// List -> each item; String -> split on commas and/or semicolons. Invalid keys are logged and ignored.
	private List<String> parseKeys(def raw) {
		if (raw == null) return []
		Collection items = (raw instanceof Collection) ? raw : [raw]
		List<String> keys = []
		items.findAll { it != null }.each { item ->
			item.toString().split(/[,;]/).each { part ->
				String k = part.trim().toUpperCase()
				if (!k || k == "NULL") return
				if (k ==~ JIRA_KEY_PATTERN) keys << k
				else println "[JIRA SYNC] WARNING: ignoring invalid Jira key '" + part.trim() + "'"
			}
		}
		return keys
	}

	private String keyFromTcFile(String testCaseId) {
		File tcFile = new File(RunConfiguration.getProjectDir() + "/" + testCaseId + ".tc")
		if (!tcFile.exists()) {
			println "[JIRA SYNC] WARNING: .tc file not found: " + tcFile.getAbsolutePath()
			return null
		}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
			return keyFromPluginLink(factory.newDocumentBuilder().parse(tcFile))
		} catch (Exception e) {
			println "[JIRA SYNC] WARNING: Parsing .tc file failed: " + e.getMessage()
			return null
		}
	}

	private String keyFromPluginLink(Document doc) {
		try {
			NodeList entities = doc.getElementsByTagName("integratedEntity")
			for (int i = 0; i < entities.getLength(); i++) {
				Element entity = (Element) entities.item(i)
				String product = entity.getElementsByTagName("productName").item(0)?.getTextContent()?.trim()
				if (!"JIRA".equalsIgnoreCase(product)) continue

				NodeList entries = entity.getElementsByTagName("entry")
				for (int e = 0; e < entries.getLength(); e++) {
					Element entry = (Element) entries.item(e)
					String key = entry.getElementsByTagName("key").item(0)?.getTextContent()?.trim()
					if ("integratedValue" != key) continue

					String value = entry.getElementsByTagName("value").item(0)?.getTextContent()
					def issue = new JsonSlurper().parseText(value)?.jiraIssue
					String jiraKey = issue?.key
					def linkedType = issue?.fields?.issuetype
					println "[JIRA SYNC] Plugin link " + jiraKey + ": issue type in .tc = " +
							(linkedType ? linkedType.name + " (id " + linkedType.id + ")" : "<not stored by plugin>") +
							", status in .tc = " + (issue?.fields?.status?.name ?: "<not stored>") + " (saved at link time, may be outdated)"
					if (jiraKey) return jiraKey
				}
			}
		} catch (Exception e) {
			println "[JIRA SYNC] WARNING: Could not read plugin link: " + e.getMessage()
		}
		return null
	}

	// ---- REST ---------------------------------------------------------------

	private String transitionIssue(String baseUrl, String authHeader, String jiraKey, String transitionId, String resolution) {
		String endpoint = baseUrl + "/rest/api/2/issue/" + jiraKey + "/transitions"
		try {
			// Read the transition's screen fields to know whether a Resolution is needed/allowed
			Map meta = http("GET", endpoint + "?transitionId=" + transitionId + "&expand=transitions.fields", authHeader, null)
			if (meta.code != 200) return "failed (HTTP " + meta.code + " reading transition)"
			def tr = new JsonSlurper().parseText(meta.body).transitions?.find { it.id?.toString() == transitionId }
			if (!tr) {
				println "[JIRA SYNC] FAILURE: transition " + transitionId + " is not available for " + jiraKey + " from its current status."
				logValidTransitions(baseUrl, authHeader, jiraKey)
				return "failed (transition " + transitionId + " not available)"
			}

			Map body = [transition: [id: transitionId]]
			def resField = tr.fields?.resolution
			if (resField) {
				List<String> allowed = (resField.allowedValues ?: []).collect { it.name?.toString() }
				String match = resolution ? allowed.find { it.equalsIgnoreCase(resolution) } : null
				if (match) {
					body.fields = [resolution: [name: match]]
				} else if (resField.required) {
					println "[JIRA SYNC] FAILURE: transition '" + tr.name + "' requires a Resolution. Configured: '" + (resolution ?: "") +
							"'. Allowed values: " + allowed + " - set RESOLUTION_<STATUS> to one of these."
					return "failed (resolution required)"
				} else if (resolution) {
					println "[JIRA SYNC] WARNING: resolution '" + resolution + "' not in allowed values " + allowed + " - transitioning without it."
				}
			} else if (resolution) {
				println "[JIRA SYNC] Transition '" + tr.name + "' has no Resolution field - resolution not sent."
			}

			// Other required fields on the transition screen that Jira will reject if empty
			List<String> otherRequired = (tr.fields ?: [:]).findAll { k, f -> f.required && k != "resolution" && !f.hasDefaultValue }.collect { k, f -> f.name ?: k }
			if (otherRequired) println "[JIRA SYNC] WARNING: transition '" + tr.name + "' has other required fields: " + otherRequired

			String payload = JsonOutput.toJson(body)
			println "[JIRA SYNC] POST " + endpoint + " " + payload
			Map resp = http("POST", endpoint, authHeader, payload)
			println "[JIRA SYNC] HTTP Response Code: " + resp.code

			if (resp.code == 204 || resp.code == 200) {
				println "[JIRA SYNC] SUCCESS: " + jiraKey + " moved via '" + tr.name + "' (id " + transitionId + ") -> now '" + tr.to?.name + "'."
				return "updated"
			}
			println "[JIRA SYNC] FAILURE: transition of " + jiraKey + " - HTTP " + resp.code
			return "failed (HTTP " + resp.code + ")"
		} catch (Exception e) {
			println "[JIRA SYNC] ERROR: REST request failed: " + e.getClass().getName() + ": " + e.getMessage()
			println "[JIRA SYNC] Diagnosis: " + diagnoseException(e)
			return "failed (" + e.getClass().getSimpleName() + ")"
		}
	}

	private Map http(String method, String endpoint, String authHeader, String body) {
		HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection()
		try {
			conn.setRequestMethod(method)
			conn.setConnectTimeout(15000)
			conn.setReadTimeout(30000)
			conn.setRequestProperty("Authorization", authHeader)
			conn.setRequestProperty("Accept", "application/json")
			if (body != null) {
				conn.setRequestProperty("Content-Type", "application/json")
				conn.setDoOutput(true)
				conn.getOutputStream().withStream { it.write(body.getBytes("UTF-8")) }
			}
			println "[JIRA SYNC] HTTP " + method + " " + endpoint
			int code = conn.getResponseCode()
			InputStream s = code >= 400 ? conn.getErrorStream() : conn.getInputStream()
			String text = s ? s.getText("UTF-8") : ""

			String seraph = conn.getHeaderField("X-Seraph-LoginReason")
			String aUser = conn.getHeaderField("X-AUSERNAME")
			String wwwAuth = conn.getHeaderField("WWW-Authenticate")
			String ctype = conn.getHeaderField("Content-Type") ?: ""
			println "[JIRA SYNC]   -> HTTP " + code + " " + (conn.getResponseMessage() ?: "") +
					" | X-Seraph-LoginReason=" + seraph + " | X-AUSERNAME=" + aUser +
					(wwwAuth ? " | WWW-Authenticate=" + wwwAuth : "")
			if (code >= 400) {
				println "[JIRA SYNC]   -> Body: " + summarizeBody(text, ctype)
				String d = diagnose(code, seraph, text)
				if (d) println "[JIRA SYNC]   -> Diagnosis: " + d
			}
			return [code: code, body: text, seraph: seraph]
		} finally {
			conn.disconnect()
		}
	}

	// ---- Diagnostics --------------------------------------------------------

	// Verifies the credentials with GET /myself. Returns true when logged in.
	private boolean checkLogin(String baseUrl, String authHeader) {
		try {
			Map resp = http("GET", baseUrl + "/rest/api/2/myself", authHeader, null)
			if (resp.code == 200) {
				def me = new JsonSlurper().parseText(resp.body)
				println "[JIRA SYNC] Login OK: name='" + (me.name ?: me.accountId) + "' displayName='" + me.displayName +
						"' email='" + me.emailAddress + "' active=" + me.active
				return true
			}
			println "[JIRA SYNC] Login FAILED: HTTP " + resp.code + " (X-Seraph-LoginReason=" + resp.seraph + ")"
		} catch (Exception e) {
			println "[JIRA SYNC] Login FAILED: " + e.getClass().getName() + ": " + e.getMessage()
			println "[JIRA SYNC] Diagnosis: " + diagnoseException(e)
		}
		return false
	}

	// HTML error pages: title + message paragraphs only. JSON/text: first 1000 chars.
	private String summarizeBody(String text, String contentType) {
		if (!text) return "<empty>"
		if (contentType.contains("html") || text.trim().startsWith("<")) {
			String title = (text =~ /(?is)<title>(.*?)<\/title>/).with { it.find() ? it.group(1).trim() : "" }
			List<String> msgs = []
			def m = text =~ /(?is)<p[^>]*>(.*?)<\/p>/
			while (m.find()) {
				String t = m.group(1).replaceAll(/<[^>]+>/, "").replaceAll(/\s+/, " ").replace("&quot;", "\"").replace("&amp;", "&").trim()
				if (t && !t.startsWith("Go to") && t != "Warning:") msgs << t
			}
			return "[HTML] " + title + (msgs ? " | " + msgs.join(" | ") : "")
		}
		return text.length() > 1000 ? text.substring(0, 1000) + "...<truncated>" : text
	}

	private String diagnose(int code, String seraph, String body) {
		String b = body ?: ""
		if ("AUTHENTICATION_DENIED".equals(seraph) || b.contains("AUTHENTICATION_DENIED"))
			return "Account is locked behind CAPTCHA after too many failed logins - Jira rejects all Basic logins without checking the password. " +
					"Log in once via browser as this user (solve CAPTCHA) or ask a Jira admin to reset the failed login count. Use a Personal Access Token to avoid this."
		if ("AUTHENTICATED_FAILED".equals(seraph) || b.contains("AUTHENTICATED_FAILED"))
			return "Wrong username or password/token. Re-enter the password/token in the Jira plugin settings or set it via class config / GlobalVariable."
		if (code == 401)
			return "Not authenticated. Jira Cloud: email + API token (Basic). Jira DC: PAT with empty username (Bearer), or username + password (Basic) if Basic auth is allowed."
		if (code == 403)
			return "Authenticated but not allowed, or Basic auth blocked by the server/admin."
		if (code == 404)
			return "Issue not found or this user has no Browse permission on its project."
		if (code == 400)
			return "Request rejected - usually the transition ID is not valid from the issue's current status, or a required field/screen."
		return null
	}

	private String diagnoseException(Exception e) {
		String all = e.toString() + " " + (e.getCause()?.toString() ?: "")
		if (all.contains("PKIX") || e instanceof javax.net.ssl.SSLException)
			return "SSL certificate not trusted - add the Jira server's (company) certificate to the Java truststore Katalon uses."
		if (e instanceof UnknownHostException) return "Host name cannot be resolved - check the base URL / VPN / DNS."
		if (e instanceof ConnectException) return "Connection refused - check the base URL, port, VPN or proxy."
		if (e instanceof SocketTimeoutException) return "Timed out - Jira slow or blocked by a proxy/firewall."
		return "See exception above."
	}
}
