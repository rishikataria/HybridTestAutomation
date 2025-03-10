package tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.codoid.products.fillo.Recordset;
import com.mongodb.DB;
import core.classes.GetObjectRepoAsJson;
import core.classes.ReadTestCases;
import core.classes.SingletonTestObject;
import core.classes.TestCaseObject;
import core.classes.TestDataObject;
import core.utilities.GenericMethodsLib;
import core.utilities.httpClientWrap;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import tpt.googleAPI.GoogleSheetUtils;
import tpt.googleAPI.TestDataFromGoogleSheet;


public class TestClass_Utils {

	Logger logger = Logger.getLogger(TestClass_Utils.class.getName());

	/** Get repo according to supplied channel type.
	 * 
	 * @param suite_type
	 * @return
	 */
	public JSONObject getObjectRepoAsJson_TestSpecific(String channel_type)
	{
		JSONObject jsonObjectRepo = null;

		if(channel_type.trim().equalsIgnoreCase("desktop"))
		{
			/**get React/Revamp object repository as JSON object */
			String objectRepoReact=SuiteClass.AUTOMATION_HOME.concat("/object_repository/desktopSiteObjectRepository/desktopSite_ObjectRepository_Desktop_Revamp.xls");
			JSONObject jsonObjectRepoReact=new GetObjectRepoAsJson().getObjectRepoAsJson(objectRepoReact);

			String objectRepoPhp=SuiteClass.AUTOMATION_HOME.concat("/object_repository/desktopSiteObjectRepository/desktopSite_ObjectRepository_PHP.xls");
			JSONObject jsonObjectRepoPhp=new GetObjectRepoAsJson().getObjectRepoAsJson(objectRepoPhp);

			/** combine both repo - for react */
			jsonObjectRepo=GenericMethodsLib.mergeJSONObject(jsonObjectRepoPhp, jsonObjectRepoReact);
		}
		else if (channel_type.trim().equalsIgnoreCase("mobile"))
		{
			/** get object repository as json object for mobile */
			String objectRepo = SuiteClass.AUTOMATION_HOME.concat("/object_repository/mobileSiteObjectRepository/mobileSite_ObjectRepository.xls");
			jsonObjectRepo = new GetObjectRepoAsJson().getObjectRepoAsJson(objectRepo);
		}
		else if (channel_type.trim().equalsIgnoreCase("vsm"))
		{
			/** get object repository as json object for vsm */
			String objectRepo = SuiteClass.AUTOMATION_HOME.concat("/object_repository/vsmObjectRepository/vsm_ObjectRepository.xls");
			jsonObjectRepo = new GetObjectRepoAsJson().getObjectRepoAsJson(objectRepo);
		}
		else
		{
			System.err.println(" *********  repo is not handled for this:  "+channel_type);
		}

		return jsonObjectRepo;
	}

	/** Get repo according to supplied channel type.
	 * 
	 * @param suite_type
	 * @return
	 */
	public JSONObject getObjectRepoAsJson_TestSpecificFromGoogle(String channel_type)
	{
		JSONObject jsonObjectRepo = null;
		GoogleSheetUtils googleSheetUtils= new GoogleSheetUtils();

		if(channel_type.trim().equalsIgnoreCase("desktop"))
		{
			List<HashMap<String, String>> listOfRepoRevamp=new ArrayList<>();

			/**get React/Revamp object repository as JSON object */
			listOfRepoRevamp = googleSheetUtils.getDataFromGoogle(
					GenericMethodsLib.googleConfigurationProperties.getProperty("object_repo_sheet").toString(), 
					GenericMethodsLib.googleConfigurationProperties.getProperty("object_repo_range").toString());

			JSONObject jsonObjectRepoReact = googleSheetUtils.getObjectRepoAsJSON(listOfRepoRevamp);
			jsonObjectRepo=jsonObjectRepoReact;
		}		
		else
		{
			System.err.println(" *********  repo is not handled for this:  "+channel_type);
		}

		return jsonObjectRepo;
	}

	/** Get test data for the test case according to channel type
	 * 
	 * @param suite_type
	 * @return
	 */
	public List<TestCaseObject> getRunnableTestCaseObjects_ChannelSpecific(String channel_type){
		List<TestCaseObject> testCaseObjectList=null;
		TestDataObject dataSheet = new TestDataObject();

		Recordset recordset =null;
		List<HashMap<String, String>> testDataList=null;

		try{
			if(channel_type.trim().equalsIgnoreCase("desktop")){

				recordset=dataSheet.readTestDataSheet
						(System.getProperty("user.dir").concat("/tc_data/desktopSite/desktopSite_Test_Data.xls"), 
								"Select * from Test_Data");

				testDataList = new TestDataObject().getTestDatasheet(recordset); 

				testCaseObjectList = dataSheet.getUpdatedTestCasesObjectList(testDataList, 
						new ReadTestCases().getRunnableTestCaseObjects(
								System.getProperty("user.dir").concat("/tc_cases/desktopSite/desktopSite_Test_Cases.xls")
								));

			}else if(channel_type.trim().equalsIgnoreCase("mobile")){

				recordset=dataSheet.readTestDataSheet
						(System.getProperty("user.dir").concat("/tc_data/mobileSite/mobileSite_Test_Data.xls"), 
								"Select * from Test_Data");

				testDataList = new TestDataObject().getTestDatasheet(recordset);

				testCaseObjectList = dataSheet.getUpdatedTestCasesObjectList(testDataList, 
						new ReadTestCases().getRunnableTestCaseObjects(
								System.getProperty("user.dir").concat("/tc_cases/mobileSite/mobileSite_Test_Cases.xls")
								));

			}else if(channel_type.trim().equalsIgnoreCase("vsm")) {
				testCaseObjectList =  new ReadTestCases().getRunnableTestCaseObjects(
						System.getProperty("user.dir").concat("/tc_cases/vsmSite/vsm_Test_Cases.xls"));

				/** Deserialize object file and storing it into list of map. Then getting the runnable data driven test cases object list*/
				List<HashMap<String, String>> listOfMap=GenericMethodsLib.dataObjectToListOfMap(testCaseObjectList);

				testCaseObjectList = new TestDataObject().getUpdatedTestCasesObjectList(listOfMap, testCaseObjectList);
			}
		}catch(Exception e){
			logger.error("Unable to get the updated test data object list "+e);
		}
		logger.debug(": Test case objects are updated, total: "+testCaseObjectList.size());

		return testCaseObjectList;
	}

	/** Get test data for the test case according to channel type
	 * 
	 * @param suite_type
	 * @return
	 */
	public List<TestCaseObject> getRunnableTestCaseObjects_ChannelSpecific_FromGoogle(String channel_type){

		List<TestCaseObject> testCaseObjectList= new ArrayList<>();

		TestDataObject dataSheet = new TestDataObject();
		TestDataFromGoogleSheet testDataSheet= new TestDataFromGoogleSheet();

		List<HashMap<String, String>> testDataList= new ArrayList<>();

		try{
			if(channel_type.trim().equalsIgnoreCase("desktop")){

				testDataList=testDataSheet.getTestDataFromGoogle(
						GenericMethodsLib.googleConfigurationProperties.getProperty("test_data_sheet_id").toString(),
						GenericMethodsLib.googleConfigurationProperties.getProperty("test_data_sheet_range").toString());

				testCaseObjectList = dataSheet.getUpdatedTestCasesObjectList(testDataList, 
						new ReadTestCases().getRunnableTestCasesObjects(
								GenericMethodsLib.googleConfigurationProperties.getProperty("test_case_sheet_id").toString(),
								GenericMethodsLib.googleConfigurationProperties.getProperty("test_case_summary_range").toString(), 
								GenericMethodsLib.googleConfigurationProperties.getProperty("test_case_step_sheet_range").toString()));

			}else {
				logger.error("Unable to get the updated test data object list of supplied channel type: "+channel_type);
			}
		}catch(Exception e){
			logger.error("Unable to get the updated test data object list ", e);
		}
		logger.debug(": Test case objects are updated");

		return testCaseObjectList;
	}

	/** this method get the final results either from map or from mongodb and converts that into a List of TestCaseObjects - which is 
	 * required by writeTestObjectResults_UsingPoI method.
	 * 
	 * @param is_parallel_execution
	 */
	public boolean getUpdatedTestCaseResultsListMongoJack(String is_parallel_execution) {

		try {

			List<TestCaseObject> updatedTestCaseObjectList = new ArrayList<>();

			/** convert the testcaseobject map in a list -- as desired by write results method -- only in case of parallel execution */
			if(is_parallel_execution.trim().equalsIgnoreCase("yes")) {

				/** get result list in case of execution via mongodb */
				if(SingletonTestObject.getSingletonTestObject().isScale_execution_via_mongo() && SingletonTestObject.getSingletonTestObject().isHubMachine()) {

					logger.info("fetching all documents for collecting results from mongo - at hub ... ");

					/** get all documents from mongo and iterate them */
					DB db = SingletonTestObject.getSingletonTestObject().getMongoDBClientConnnection().getDB("Automation");

					/** Mapping from testcasesObject to mongo document */
					JacksonDBCollection<TestCaseObject, String> collection = JacksonDBCollection.wrap(db.getCollection("testcaseobjects"), TestCaseObject.class,
							String.class);

					/**Get the Query result from the mongo */
					DBCursor<TestCaseObject> listofTestCases= collection.find();

					for(TestCaseObject testCaseObject : listofTestCases)
					{
						/** finally add in a list*/
						updatedTestCaseObjectList.add(testCaseObject);
					}

					logger.info("result list is created from all mongo documents  ... ");
				}

				/** get result list in case of execution via maps */
				else {
					for(Entry<String, TestCaseObject> entry : SingletonTestObject.getSingletonTestObject().getTestCaseObjectMap().entrySet()){
						updatedTestCaseObjectList.add(entry.getValue());
					}	
				}

				/** update singleton test object  */
				SingletonTestObject.getSingletonTestObject().setTestCaseObjectList(updatedTestCaseObjectList);
			}

			return true;

		}catch (Exception e) {
			logger.error(e.getMessage(), e);

			return false;
		}
	}

	/**
	 * create avd dynamically in a separate thread before starting tests .. 
	 */
	public void manageAVDDynamically() {

		ExecutorService ex = SingletonTestObject.getSingletonTestObject().getMonitoringExecutorService();

		/** creating a runnable to unblick the main thread ..*/
		ex.submit(new Runnable() {

			@Override
			public void run() {

				/** running this whole code in runnable thread so that it doesn't block the execution .. */
				try{
					Set<Callable<Object>> set = new HashSet<>();

					for(int i=0; i< SingletonTestObject.getSingletonTestObject().getTestCaseObjectList().size(); i++) {

						try {
							TestCaseObject testCaseObject = SingletonTestObject.getSingletonTestObject().getTestCaseObjectList().get(i);

							String appiumRun = "";
							String browserType = testCaseObject.getTestCaseSupportedBrowserType();
							try{appiumRun = new JSONObject(browserType).getString("appiumRun").trim();}catch (JSONException e) {};

							if(appiumRun.equalsIgnoreCase("Yes")) {
								Callable<Object> tasks = new Callable<Object>() {

									@Override
									public Object call() throws Exception {

										/** first an existing avd and then create it -- clean setup */
										new GenericMethodsLib().deleteAVDDynamically(testCaseObject.getTestCaseId());
										new GenericMethodsLib().createAVDDynamically(testCaseObject.getTestCaseId());

										return true;
									}
								};

								set.add(tasks);					
							}

						}catch (Exception e) {
							logger.error(e.getMessage(), e);	
						}
					}

					/** submitting the set of task to executor */
					if(set.size() > 0) {
						ex.invokeAll(set);
						logger.info(SuiteClass.UNIQ_EXECUTION_ID.get() + " : " + " submitted task to create avds .. ");
					}

				}catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
	}

	/**
	 * this is just a hack to handle notification in emulator in app.
	 * @param driver
	 * @return 
	 */
	public void handlePreConditions(TestCaseObject testCaseObject, WebDriver driver)
	{

		/** handle alert only if its not handled only incase of appium driver */
		if(driver instanceof AppiumDriver<?> && testCaseObject.isHandleNotificationForAppiumRequired()) {	

			if(!testCaseObject.isAppiumDriverAlertHandled()) {
				try {
					String xpath = "/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/rO/android.widget.ScrollView/android.widget.LinearLayout/android.widget.Button[1]";

					((AndroidDriver<?>)driver).context("NATIVE_APP");

					///** send a get request to get page source rather sending from appium driver. */
					//String appiumDriver=testCaseObject.getAppiumDriverURL()+"/session/"+testCaseObject.getAppiumDriverSessionId()+"/source";
					//String pageSource = (String) new httpClientWrap().sendGetRequestWithParams(appiumDriver).get("response");

					//click only if there is any alert in page source
					if(driver.getPageSource().contains("<android.widget.Button index=\"0\" text=\"BLOCK\" class=\"android.widget.Button\" package=\"com.android.chrome\"")) {
						//if(pageSource.contains("<android.widget.Button index=\"0\" text=\"BLOCK\" class=\"android.widget.Button\" package=\"com.android.chrome\"")) {

						logger.info(SuiteClass.UNIQ_EXECUTION_ID.get() + " - Alert Found. " );
						((AndroidDriver<?>)driver).findElement(By.xpath(xpath)).click();	

						testCaseObject.setAppiumDriverAlertHandled(true);
					}

					((AndroidDriver<?>)driver).context("CHROMIUM");

				}catch (Exception e) {
					try{((AndroidDriver<?>)driver).context("CHROMIUM");}catch (Exception e1) {logger.error(SuiteClass.UNIQ_EXECUTION_ID.get() + " - exception while handling alert: "+e.getMessage(), e);}
				}
			}
		}
	}

	/** This method checks if appium server and chromedriver is up and running before every step with a timeout of 1 min by making http request
	 * to appium server and get 200 OK response if not that means either of appium and chrome driver is not up or both are not up.
	 * There is no direct way to check if chromedriver is running so we gonna call this request -
	 * http://127.0.0.1:51172/wd/hub/session/882ea82b-2dda-4bd4-9bde-e3467f5628c3/contexts
	 * which further make a request to chromedriver and gets the response, if no response then don't proceed for test.  
	 * 
	 * @return
	 */
	public synchronized AtomicBoolean ifProceedForTestWithAppiumServer(TestCaseObject testCaseObject) {

		AtomicBoolean proceed = new AtomicBoolean(false);

		try {
			String url = testCaseObject.getAppiumDriverURL().replace("0.0.0.0", "localhost")+"/session/"+testCaseObject.getAppiumDriverSessionId().toString()+"/contexts";

			/** expected response: 
		 {
    			"value": [
        		"NATIVE_APP",
        		"CHROMIUM"
    					]
		}
			 */

			HashMap<Object, Object> response = new httpClientWrap().sendGetRequestWithParams(url);

			/** may need to add further one more check on response like - must need to receive chromium in response - to make sure chromedriver is up */
			if( (response == null || response.isEmpty()) || (Integer)response.get("statuscode") != 200 || response.get("response").toString().isEmpty()) {

				proceed = new AtomicBoolean(false);
				try{logger.info(SuiteClass.UNIQ_EXECUTION_ID.get()+" : pre-condition failed - with appium url: " +url + " not proceeding to further tests. " + " received map: "+response);}catch (Exception e) {}
			}
			else {
				proceed = new AtomicBoolean(true);
			}
		}catch (Exception e) {
			logger.error(SuiteClass.UNIQ_EXECUTION_ID.get() + " : " + e.getMessage(), e);
		}
		return proceed;
	}


}
