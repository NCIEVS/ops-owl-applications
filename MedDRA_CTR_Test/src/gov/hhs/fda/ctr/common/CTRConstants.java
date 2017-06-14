package gov.hhs.fda.ctr.common;

public class CTRConstants {

	// COMMON
	public static final String COM_APPEND_BLANK_SPACE = " ";
	public static final String COM_APPEND_COLON = ":";
	public static final String COM_APPEND_FORWARD_SLASH = "/";
	public static final String COM_PROTOCOL_HTTPS = "https";
	public static final String COM_PROTOCOL_HTTP = "http";
	public static final String COM_PROPERTIES_FILE = "../conf/CTRCommon.properties";
	public static final String COM_PROPERTIES_FILE_PENTAHO = "CTRCommon.properties";
	public static final String COM_JBPM_CONSOLE_URL_SUFFIX = "jbpm-console";
	// exception messages
	public static final String COM_ERROR_MSG_PROPERTY_FILE_LOAD_FAILURE = "Error loading properties file";
	public static final String COM_CXF_CLIENT_CONFIG_FILE_NAME = "CXFClientConfig.xml";
	public static final String COM_WORK_ITEM_NAME_END = "END";
	public static final String COM_CTR_WORKFLOW_ID = "gov.hhs.fda.ctr.workflow";
	public static final String COM_PRIMARY_CTR_DATASOURCE_NAME = "java:comp/env/jdbc/testDS1";

	public static final String COM_PARAM_NAME_WORK_ITEM = "workitemID";
	public static final String COM_PARAM_NAME_COMPONENT_NAME = "componentName";
	public static final String COM_PARAM_NAME_RESPONSE_STATUS = "responseStatus";
	public static final String COM_PARAM_NAME_ERROR_TYPE = "errorType";
	public static final String COM_PARAM_NAME_ERROR_CODE = "errorCode";
	public static final String COM_PARAM_NAME_ERROR_DESCRIPTION = "errorDescription";
	public static final String COM_PARAM_NAME_CENTER_NAME = "centerName";
	public static final String COM_PARAM_NAME_PRODUCT_APP_NUM = "productAppNum";
	public static final String COM_PARAM_NAME_SUBMISSION_TYPE = "submissionType";
	public static final String COM_PARAM_NAME_STUDYID = "studyID";
	public static final String COM_PARAM_NAME_DATA_TYPE = "dataType";
	public static final String COM_PARAM_NAME_TRANSMIT_NUM = "transmitNum";
	public static final String COM_PARAM_NAME_CHECKSUM = "checksum";
	public static final String COM_PARAM_NAME_WORKITEM_PROCESS_START_TIME = "workItemProcessStartTime";
	public static final String COM_PARAM_NAME_WORKITEM_PROCESS_END_TIME = "workItemProcessEndTime";
	public static final String COM_PARAM_NAME_IS_LEGACY = "isLegacy";
	public static final String COM_PARAM_NAME_WORK_ITEM_TO_EXECUTE = "workItemToExecute";
	public static final String COM_PARAM_NAME_IS_SYSTEM_ERROR_RESOLVED = "isSystemErrorResolved";
	public static final String COM_PARAM_NAME_COMMENT_SUPPORT_TASK = "comment";
	public static final String COM_PARAM_NAME_JBPM_USERNAME = "jbpmUsername";
	public static final String COM_PARAM_NAME_IS_PING_SUCCESSFUL = "isPingSuccessful";
	public static final String COM_PARAM_NAME_PING_TIMER_PERIOD = "pingTimerPeriod";
	public static final String COM_PARAM_NAME_IS_SERVICE_OUTPUT_AVAILABLE = "isServiceOutputAvailable";
	public static final String COM_PARAM_NAME_PING_TIME = "pingTime";
	public static final String COM_PARAM_NAME_IS_SERVICE_BROUGHT_UP = "isServiceBroughtUp";
	public static final String COM_PARAM_NAME_CTR_PROCESS_INSTANCE_ID = "ctrProcessInstanceID";
	public static final String COM_PARAM_NAME_SDTM_DATASET_LOCATION = "sdtmDatasetLocation";

	public static final String COM_PARAM_NAME_PING_RESPONSE_STATUS = "pingResponseStatus";
	public static final String COM_PARAM_NAME_PING_ERROR_TYPE = "pingErrorCode";
	public static final String COM_PARAM_NAME_PING_ERROR_CODE = "pingErrorType";
	public static final String COM_PARAM_NAME_PING_ERROR_DESCRIPTION = "pingErrorDescription";
	public static final String COM_PARAM_NAME_PING_COMMENT = "pingComment";
	public static final String COM_PARAM_NAME_PENTAHO_STUDY_ID = "pentahoStudyID";
	//STG_STUDY_ID

	public static final String COM_SEQUENCE_NAME_CTR_PROCESSINSTANCE_SEQ = "CTR_PROCESSINSTANCE_SEQ";
	public static final String COM_SEQUENCE_NAME_CTR_BUS_NODEINSTANCE_SEQ = "CTR_BUS_NODEINSTANCE_SEQ";
	public static final String COM_SEQUENCE_NAME_CTR_BUS_NODE_OUTPUT_SEQ = "CTR_BUS_NODE_OUTPUT_SEQ";

	public static final String COM_PARAM_VALUE_RESPONSE_STATUS_SUCCESS = "SUCCESS";
	public static final String COM_PARAM_VALUE_RESPONSE_STATUS_FAILURE = "FAILURE";
	public static final String COM_PARAM_VALUE_BUSINESS_ERROR = "BUSINESS_ERROR";
	public static final String COM_PARAM_VALUE_SYSTEM_ERROR = "SYSTEM_ERROR";
	public static final String COM_PARAM_VALUE_SYSTEM_ERROR_RESOLVED = "true";
	public static final String COM_PARAM_VALUE_SYSTEM_ERROR_NOT_RESOLVED = "false";
	public static final String COM_PARAM_VALUE_PING_SUCCESSFUL = "true";
	public static final String COM_PARAM_VALUE_PING_NOT_SUCCESSFUL = "false";
	public static final String COM_PARAM_VALUE_SERVICE_OUTPUT_AVAILABLE = "true";
	public static final String COM_PARAM_VALUE_SERVICE_OUTPUT_NOT_AVAILABLE = "false";
	public static final String COM_PARAM_VALUE_TRUE = "true";
	public static final String COM_PARAM_VALUE_FALSE = "false";
	public static final String COM_PARAM_NAME_STAGE_OUTBOUND_FOLDER = "stageOutboundFolder";

	public static final String COM_PROPERTY_NAME_ZIPPED_DATASET_FOLDER = "zipped.dataset.folder";
	public static final String COM_PROPERTY_NAME_ZIPPED_DATASET_PROCESSED = "zipped.dataset.processed";
	public static final String COM_PROPERTY_NAME_EXPLODED_FOLDER = "exploded.folder";
	public static final String COM_PROPERTY_NAME_STAGE_OUTBOUND_FOLDER = "stage.outbound.folder";
	public static final String COM_PROPERTY_NAME_FTP_OUTBOUND_FOLDER = "ftp.outbound.folder";
	public static final String COM_PROPERTY_NAME_CONTROL_FILE_PENDING_FOLDER = "control.file.pending.folder";
	public static final String COM_PROPERTY_NAME_CONTROL_FILE_PROCESSED_FOLDER = "control.file.processed.folder";
	public static final String COM_PROPERTY_NAME_CONTROL_FILE_POLL_INTERVAL_SECONDS = "control.file.poll.interval.seconds";
	public static final String COM_PROPERTY_NAME_JBPM_USERNAME = "jbpm.username";
	public static final String COM_PROPERTY_NAME_JBPM_PASSWORD = "jbpm.password";
	public static final String COM_PROPERTY_NAME_MAIL_SMTP_HOST = "mail.smtp.host";
	public static final String COM_PROPERTY_NAME_MAIL_CTR_FROM = "mail.ctr.from";
	public static final String COM_PROPERTY_NAME_MAIL_CTR_SUPPORT_TO = "mail.ctr.support.to";
	public static final String COM_PROPERTY_NAME_PING_TIMER_PERIOD = "ping.timer.period";
	public static final String COM_PROPERTY_NAME_DATABASE_PROVIDER = "database.type";
	public static final String COM_PROPERTY_NAME_PENTAHO_HOME = "pentaho.home";
	public static final String COM_PROPERTY_NAME_PENTAHO_USERNAME = "pentaho.username";
	public static final String COM_PROPERTY_NAME_PENTAHO_PASSWORD = "pentaho.password";

	// CTR INTERFACE SERVICE
	public static final String CIS_COMPONENT_NAME = "CTR_INTERFACE_COMPONENT";
	public static final String CIS_REGEX_TRANSMIT_NUMBER = "(Legacy|Regulatory)+_Transmit+_[0-9]{3}";
	public static final String CIS_REGEX_ISLEGACY = "(true|false)";
	public static final String CIS_CHECK_LEGACY_STRING = "Legacy";
	public static final String CIS_CHECK_REGULATORY_STRING = "Regulatory";
	public static final String CIS_BUSERRCODE_CANNOT_PARSE_CONTROL_FILE = "CIS10010";
	public static final String CIS_BUSERRCODE_ONE_OR_MORE_CONTROL_FILE_DATA_VALUES_INCORRECT = "CIS10020";
	public static final String CIS_BUSERRCODE_CENTER_NAME_REQUIRED = "CIS10021";
	public static final String CIS_BUSERRCODE_PRODUCT_APP_NUM_REQUIRED = "CIS10022";
	public static final String CIS_BUSERRCODE_SUBMISSION_TYPE_REQUIRED = "CIS10023";
	public static final String CIS_BUSERRCODE_STUDY_ID_REQUIRED = "CIS10024";
	public static final String CIS_BUSERRCODE_DATA_TYPE_REQUIRED = "CIS10025";
	public static final String CIS_BUSERRCODE_TRANSMIT_NUM_FORMAT = "CIS10026";
	public static final String CIS_BUSERRCODE_CHECKSUM_REQUIRED = "CIS10027";
	public static final String CIS_BUSERRCODE_ISLEGACY_FORMAT = "CIS10028";
	public static final String CIS_BUSERRDESC_ONE_OR_MORE_CONTROL_FILE_DATA_VALUES_INCORRECT = "One of more control file data values Incorrect";
	public static final String CIS_BUSERRDESC_CENTER_NAME_REQUIRED = "Center Name is Required";
	public static final String CIS_BUSERRDESC_PRODUCT_APP_NUM_REQUIRED = "Product Application Number is Required";
	public static final String CIS_BUSERRDESC_SUBMISSION_TYPE_REQUIRED = "Submission Type is Required";
	public static final String CIS_BUSERRDESC_STUDY_ID_REQUIRED = "Study ID is Required";
	public static final String CIS_BUSERRDESC_DATA_TYPE_REQUIRED = "Data Type is Required";
	public static final String CIS_BUSERRDESC_TRANSMIT_NUM_FORMAT = "Transmit Number value missing or its Format is Incorrect. Valid Format (Regular Expression) is "
			+ CIS_REGEX_TRANSMIT_NUMBER;
	public static final String CIS_BUSERRDESC_CHECKSUM_REQUIRED = "Checksum is Required";
	public static final String CIS_BUSERRDESC_ISLEGACY_FORMAT = "IsLegacy value missing or is Invalid. Valid values are "
			+ CIS_REGEX_ISLEGACY;
	public static final String CIS_SYSERRCODE_CONTROL_FILE_PENDING_FOLDER_DOES_NOT_EXIST = "CIS10110";
	public static final String CIS_SYSERRCODE_CONTROL_FILE_PROCESSED_FOLDER_DOES_NOT_EXIST = "CIS10120";
	public static final String CIS_SYSERRCODE_CTR_WORKFLOW_DOWN_OR_NOT_REACHABLE = "CIS10130";
	public static final String CIS_SYSERRCODE_OTHER_OS_ERROR = "CIS10140";
	public static final String CIS_SYSERRCODE_FTP_OUTBOUND_FOLDER_DOES_NOT_EXIST = "CIS10150";
	public static final String CIS_CONFIG_PROPERTIES_FILE_NAME = "ctr.interface.config.properties";
	public static final String CIS_CONFIG_CXF_FILE_NAME = "CXFInboundServer.xml";
	public static final String CIS_CONFIG_CXF_HOST_PROPNAME = "ctr.interface.cxf.host";
	public static final String CIS_CONFIG_CXF_PORT_PROPNAME = "ctr.interface.cxf.port";
	public static final String CIS_CXF_SOAP_SERVICE_NAME = "CTRInterfaceService";
	public static final String CIS_CXF_SOAP_MSG_FACTORY_PROP_NAME = "javax.xml.soap.MessageFactory";
	public static final String CIS_CXF_SOAP_MSG_FACTORY_PROP_VALUE = "com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl";
	public static final String CIS_CLIENT_BEAN_NAME = "ctrInterfaceClientFactory";

	// WORK-FLOW
	public static final String WRF_RESTAPI_KEY_USERNAME = "j_username";
	public static final String WRF_RESTAPI_KEY_PASSWORD = "j_password";
	public static final String WRF_RESTAPI_HOST_PROPERTY = "workflow.restapi.host";
	public static final String WRF_RESTAPI_PORT_PROPERTY = "workflow.restapi.port";

	// EXTRACT
	public static final String EXT_SERVICE_NAME = "EXTRACTION_SERVICE";
	public static final String EXT_BUSERRCODE_DUPLICATE_SUBMISSION = "EXT10010";
	public static final String EXT_BUSERRCODE_STUDY_VERSION_NOT_SEQUENTIAL = "EXT10020";
	public static final String EXT_BUSERRCODE_CHECKSUM_MISMATCH = "EXT10030";
	public static final String EXT_BUSERRCODE_UNABLE_TO_ASSEMBLE = "EXT10040";
	public static final String EXT_BUSERRCODE_ERROR_DECOMPRESSING = "EXT10050";
	public static final String EXT_BUSERRCODE_CONTROL_FILE_DATA_AND_ZIP_FILE_DIRECTORY_MISMATCH = "EXT10060";
	public static final String EXT_SYSERRCODE_ZIPPED_DATA_FOLDER_DOES_NOT_EXIST = "EXT10110";
	public static final String EXT_SYSERRCODE_EXPLODED_FOLDER_DOES_NOT_EXIST = "EXT10120";
	public static final String EXT_SYSERRCODE_STAGE_OUTBOUND_FOLDER_DOES_NOT_EXIST = "EXT10130";
	public static final String EXT_SYSERRCODE_OTHER_OS_ERROR = "EXT10140";
	public static final String EXT_SYSERRCODE_FTP_OUTBOUND_FOLDER_DOES_NOT_EXIST = "EXT10150";
	public static final String EXT_SYSERRCODE_ZIPPED_DATA_PROCESSED_DOES_NOT_EXIST = "EXT10160";
	public static final String EXT_SYSERRCODE_ERROR_MOVING_PROCESSED_ZIP_FILE = "EXT10170";
	public static final String EXT_CLIENT_BEAN_NAME = "extractionClientFactory";

	// VALIDATION
	public static final String VAL_SERVICE_NAME = "VALIDATION_SERVICE";
	public static final String VAL_CONFIG_DIR_PROPERTY = "ctr.service.validation.config.path";
	public static final String VAL_CONFIG_DEFINE_NORMAL_PROPERTY = "ctr.service.validation.config.define.normal";
	public static final String VAL_CONFIG_DEFINE_LEGACY_PROPERTY = "ctr.service.validation.config.define.legacy";
	public static final String VAL_CONFIG_SDTM_NORMAL_PROPERTY = "ctr.service.validation.config.sdtm.normal";
	public static final String VAL_CONFIG_SDTM_LEGACY_PROPERTY = "ctr.service.validation.config.sdtm.legacy";
	public static final String VAL_CLIENT_BEAN_NAME = "validationClientFactory";
	public static final String VAL_DEFINE_FILE = "define.xml";
	public static final String VAL_EVS_CDISC_ROOT = "C66830";
	public static final String VAL_EVS_CDISC_SCHEME = "NCI Thesaurus";
	public static final String VAL_EVS_MEDDRA_ID_PROPERTY = "ctr.service.validation.meddra.id";
	public static final String VAL_EVS_MEDDRA_SCHEME = "MedDRA";
	public static final String VAL_EVS_SERVICE_NAME = "EvsServiceInfo";
	//public static final String VAL_EVS_SERVICE_URL = " http://ncias-d488-v.nci.nih.gov:29080/lexevsapi60";
	public static final String VAL_EVS_SERVICE_URL = "http://lexevsapi60.nci.nih.gov/lexevsapi60";
	public static final String VAL_XPORT_EXT = ".xpt";
	public static final String VAL_SYSERRCODE_OTHER_OS_ERROR = "VAL10070";

	// ETL
	public static final String ETL_CLIENT_BEAN_NAME = "etlClientFactory";
	public static final String ETL_PENTAHO_REPOSITORY_NAME = "CTRRepository";

	// LOAD STAGE DATABASE
	public static final String SLD_SERVICE_NAME = "STAGE_LOAD_SERVICE";
	public static final String SLD_SYSERRCODE_ERROR_PARSING_AND_PERSISTING_INFORMATION_IN_DEFINE_XML = "SLD10110";
	public static final String SLD_SYSERRCODE_ERROR_CREATING_PARTITIONS_FOR_STUDY_IN_STAGE_DATABASE = "SLD10111";
	public static final String SLD_SYSERRCODE_ERROR_LOADING_SDTM_DATA_INTO_STAGE_TABLES = "SLD10112";
	public static final String SLD_SYSERRCODE_OTHER_OS_ERROR = "SLD10109";
	public static final String SLD_PENTAHO_JOB_NAME = "SDTM Stage Load";
	
	// CTR TRANSFORM
	public static final String CTM_SERVICE_NAME = "CTR_TRANSFORM_SERVICE";
	public static final String CTM_SYSERRCODE_OTHER_OS_ERROR = "CTM10109";
	public static final String CTM_PENTAHO_JOB_NAME = "SDTM CTR Transform";
	
	// CTR LOAD
	public static final String CLD_SERVICE_NAME = "CTR_LOAD_SERVICE";
	public static final String CLD_SYSERRCODE_OTHER_OS_ERROR = "CLD10109";
	public static final String CLD_PENTAHO_JOB_NAME = "CTR Load";
	
	// SDTM EXTRACT
	public static final String SXT_SERVICE_NAME = "SDTM_EXTRACT_SERVICE";
	public static final String SXT_SYSERRCODE_OTHER_OS_ERROR = "SXT10109";
	public static final String SXT_PENTAHO_JOB_NAME = "CTR SDTM Extract";

	// MAIL
	public static final String MAIL_CTR_SUPPORT_TO_NAME = "CTR Support";
	public static final String MAIL_CTR_FROM_NAME = "(CTR) CLINICAL TRIALS REPOSITORY";
	public static final String MAIL_CTR_ERROR_SUBJECT = "CTR SYSTEM ERROR";
	public static final String MAIL_CTR_PING_ERROR_SUBJECT = "CTR SERVICE DOWN";
	public static final String MAIL_CTR_ERROR_BODY = "<html><head><style type='text/css'>table.sample {	border-width: 0px;	border-spacing: 5px;	border-style: outset;	border-color: blue;	border-collapse: separate;	background-color: none;	width: 800px;}table.sample th {	border-width: 0px;	padding: 1px;	border-style: none;	border-color: gray;	-moz-border-radius: ;	font-family: Arial;}table.sample td {	border-width: 0px;	padding: 1px;	border-style: none;	border-color: gray;	-moz-border-radius: ;}</style><title>System Error Details</title></head><body>	<table class='sample'>		<tr>			<th align='center'				style='color: #2554C7; font-size: 22px; text-decoration: underline;'>SYSTEM&nbsp;&nbsp;ERROR&nbsp;&nbsp;DETAILS			</th>		</tr>	</table>	<table class='sample'>		<tr>			<th colspan='2'>&nbsp;</th>		</tr>		<tr bgcolor='#000000'>			<th align='left' colspan='2' style='color: #FFFFFF'>&nbsp;&nbsp;&nbsp;Dataset				Identification</th>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Center Name&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Product Application Number&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Submission Type&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Study ID&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Data Type&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Transmit Num&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Is Legacy&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#000000'>			<th align='left' colspan='2' style='color: #FFFFFF'>&nbsp;&nbsp;&nbsp;Error				Details</th>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Process Start Time&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Process End Time&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>CTR Component Name&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Error Type&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Error Code&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Error Description&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr>			<th colspan='2'>&nbsp;</th>		</tr>	</table>	<form method='LINK' action='%s'>		<table class='sample'>			<tr>				<th align='center'><input					style='color: #2554C7; font-weight: bold; font-size: 22px'					type='submit' value='LOGIN TO JBPM & UPDATE STATUS'></th>			</tr>		</table>	</form></body></html>";
	public static final String MAIL_CTR_PING_ERROR_BODY = "<html><head><style type='text/css'>table.sample {	border-width: 0px;	border-spacing: 5px;	border-style: outset;	border-color: blue;	border-collapse: separate;	background-color: none;	width: 800px;}table.sample th {	border-width: 0px;	padding: 1px;	border-style: none;	border-color: gray;	-moz-border-radius: ;	font-family: Arial;}table.sample td {	border-width: 0px;	padding: 1px;	border-style: none;	border-color: gray;	-moz-border-radius: ;}</style><title>CTR Service Down</title></head><body>	<table class='sample'>		<tr>			<th align='center'				style='color: #2554C7; font-size: 22px; text-decoration: underline;'>CTR&nbsp;&nbsp;SERVICE&nbsp;&nbsp;DOWN			</th>		</tr>	</table>	<table class='sample'>		<tr>			<th colspan='2'>&nbsp;</th>		</tr>		<tr bgcolor='#000000'>			<th align='left' colspan='2' style='color: #FFFFFF'>&nbsp;&nbsp;&nbsp;Dataset				Identification</th>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Center Name&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Product Application Number&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Submission Type&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Study ID&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Data Type&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Transmit Num&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Is Legacy&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#000000'>			<th align='left' colspan='2' style='color: #FFFFFF'>&nbsp;&nbsp;&nbsp;Error				Details</th>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Ping Time&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>CTR Service Name&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr>			<th colspan='2'>&nbsp;</th>		</tr>	</table>	<form method='LINK' action='%s'>		<table class='sample'>			<tr>				<th align='center'><input					style='color: #2554C7; font-weight: bold; font-size: 22px'					type='submit' value='LOGIN TO JBPM & UPDATE STATUS'></th>			</tr>		</table>	</form></body></html>";
	public static final String MAIL_CTR_INTERFACE_ERROR_BODY = "<html><head><style type='text/css'>table.sample {	border-width: 0px;	border-spacing: 5px;	border-style: outset;	border-color: blue;	border-collapse: separate;	background-color: none;	width: 800px;}table.sample th {	border-width: 0px;	padding: 1px;	border-style: none;	border-color: gray;	-moz-border-radius: ;	font-family: Arial;}table.sample td {	border-width: 0px;	padding: 1px;	border-style: none;	border-color: gray;	-moz-border-radius: ;}</style><title>System Error Details</title></head><body>	<table class='sample'>		<tr>			<th align='center'				style='color: #2554C7; font-size: 22px; text-decoration: underline;'>SYSTEM&nbsp;&nbsp;ERROR&nbsp;&nbsp;DETAILS			</th>		</tr>	</table>	<table class='sample'>		<tr>			<th colspan='2'>&nbsp;</th>		</tr>		<tr bgcolor='#000000'>			<th align='left' colspan='2' style='color: #FFFFFF'>&nbsp;&nbsp;&nbsp;Dataset				Identification</th>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Center Name&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Product Application Number&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Submission Type&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Study ID&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Data Type&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Transmit Num&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Is Legacy&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#000000'>			<th align='left' colspan='2' style='color: #FFFFFF'>&nbsp;&nbsp;&nbsp;Error				Details</th>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Process Start Time&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Process End Time&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>CTR Component Name&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Error Type&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#FFFFFF'>			<th align='right'>Error Code&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr bgcolor='#E0E0E0'>			<th align='right'>Error Description&nbsp;&nbsp;&nbsp;</th>			<td align='left'>%s</td>		</tr>		<tr>			<th colspan='2'>&nbsp;</th>		</tr>	</table>			<table class='sample'>			<tr>				<th align='center'><input type='hidden' name='jbpmURL' value='%s'></th>			</tr>		</table>	</body></html>";

	// REPORT
	public static final String REP_COMPONENT_NAME = "REPORT_COMPONENT";

	// PING
	public static final String PING_SYSERRCODE_SERVICE_DOWN = "PNG10110";

	// MOVE

	// LOAD

}
