package  c.m;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

public class GctsClientTool {

    public static String getProjectId() {
        return "mstr-sandbox-sbx-4aff";
    }

    private static volatile String tenantId = null;
    public static synchronized String getTenantId() throws Exception {

        if (null != tenantId)
            return tenantId;

        String externalId = "mstr-sandbox-sbx-durval-tenant-1";

        String projectId = getProjectId();
        TenantManager.listTenants(projectId);
        tenantId = TenantManager.lookupTenant(projectId, externalId);
        if (tenantId.length()==0)
            tenantId = TenantManager.createTenant(projectId, externalId);

        if (tenantId.length()==0) {
            throw new Exception("could not get tenant for " + externalId);
        }

        return tenantId;
    }


    public static void main(String[] args) throws Exception {

        String projectId = getProjectId();
        String tenantId = getTenantId();

        CompanyManager.listCompanies(projectId, tenantId);

        String c1name = "company 1 inc";
        String c1id = makeCompany(projectId, tenantId, c1name);
        String c2name = "My company 2";
        String c2id = makeCompany(projectId, tenantId, c2name);
        String c3name = "Threes company";
        String c3id = makeCompany(projectId, tenantId, c3name);

        String reqid = "1-0-0-";
        JobManager.listJobs(projectId, tenantId, "companyName=\""+c1id+"\"");
        for (int i = 0; i < 10; i++) {
            createJobsForCompany(projectId, tenantId, c1id, reqid + String.valueOf(i));
        }
        JobManager.listJobs(projectId, tenantId, "companyName=\""+c1id+"\"");

        reqid = "2-0-0-";
        JobManager.listJobs(projectId, tenantId, "companyName=\""+c2id+"\"");
        for (int i = 0; i < 10; i++) {
            createJobsForCompany(projectId, tenantId, c2id, reqid + String.valueOf(i));
        }
        JobManager.listJobs(projectId, tenantId, "companyName=\""+c2id+"\"");

        reqid = "333333-0-0-";
        JobManager.listJobs(projectId, tenantId, "companyName=\""+c3id+"\"");
        for (int i = 0; i < 10; i++) {
            createJobsForCompany(projectId, tenantId, c3id, reqid + String.valueOf(i));
        }
        JobManager.listJobs(projectId, tenantId, "companyName=\""+c3id+"\"");

        reqid = "2-0-0-";
        updateJobsForCompany(projectId, tenantId, c2id, reqid + String.valueOf(0));

        //sampleSearchJobs(projectId, tenantId, "count(base_compensation, [bucket(12, 20)])");
        JobManager.searchJobs(projectId, tenantId, "+title +11" , null);
        JobManager.searchJobs(projectId, tenantId, null , "mf_requisitionId=\"7101\"");
        JobManager.searchJobs(projectId, tenantId, "+title +jt" , "mf_requisitionId=\"2-0-0-5\"");
        JobManager.searchJobs(projectId, tenantId, "+title +jt77" , null);  // expect 0 found
        JobManager.searchJobs(projectId, tenantId, "+title +jt" , null);  // expect all to be found
        JobManager.searchJobs(projectId, tenantId, null , null);  // expect all to be found

        JobManager.deleteAllJobs(projectId, tenantId, "companyName=\""+c1id+"\"");
        JobManager.deleteAllJobs(projectId, tenantId, "companyName=\""+c2id+"\"");

        TenantManager.deleteTenant(projectId, tenantId);
    }

    private static void createJobsForCompany(String projectId, String tenantId, String companyid, String reqid) {

        if (JobManager.lookupJob(projectId, tenantId, reqid).length == 0) {
            JobManager.makeJob(projectId, tenantId, companyid, reqid
                    , "TESTING JOBTITLE jt title " + reqid, "description 1", "url1", "Buxton, NC, US",
                    "27920", "en-US");
        }
    }

    private static void updateJobsForCompany(String projectId, String tenantId, String companyid, String reqid) {

        String jnames[] = JobManager.lookupJob(projectId, tenantId, reqid);
        if (jnames.length > 0) {
            JobManager.updateJob(jnames[0], projectId, tenantId, companyid, reqid
                    , "UPDATE title " + reqid, "Updated description 999", "url1:u1", "Buxton, NC, US",
                    "27920", "en-US");
        }
    }

    private static String makeCompany(String projectId, String tenantId, String cmpanyName) {
        String compId = CompanyManager.lookupCompany(projectId, tenantId, cmpanyName);
        if (compId.length()==0)
            compId = CompanyManager.createCompany(projectId, tenantId, cmpanyName, cmpanyName);

        return compId;
    }

    public static void delete(String postingId) throws Exception {
        System.out.println("ddddddd ===== delete postingid " + postingId);
        String[] jobnames = JobManager.lookupJob(getProjectId(), getTenantId(), postingId);
        for (int i = 0; i < jobnames.length; i++) {
            System.out.println("delete postingid " + postingId + " has jobname " + jobnames[i]);
            JobManager.deleteJobByName(jobnames[i]);
        }
        System.out.println("delete postingid " + postingId + " deleted " + jobnames.length);
    }

    public static void update(String postingId, String jobadxml) throws Exception {

        String[] jobnames = JobManager.lookupJob(getProjectId(), getTenantId(), postingId);

        // get the elements needed from the xmlstring
        MJobUpdate ju = toJobUpdate(jobadxml);

        if (ju.languageCode.length() == 0){
            System.out.println("languageCode, not supported, " + ju);
            delete(postingId);
            return;
        }

        System.out.println(" ==== update " + ju);
        String compId = CompanyManager.lookupCompany(getProjectId(), getTenantId(), ju.companyName);
        if (compId.length() == 0 ) {
            compId = CompanyManager.createCompany(getProjectId(), getTenantId(), ju.companyName, ju.companyId);
        }

        if (jobnames.length == 0 ) {
            // create the job
            JobManager.makeJob(getProjectId(), getTenantId(), compId, postingId, ju.title, ju.description, ju.applicationInfo, ju.address1(), ju.address2(), ju.languageCode);
        } else {
            for (int i = 0; i < jobnames.length; i++) {
                JobManager.updateJob(jobnames[i], getProjectId(), getTenantId(), compId, postingId, ju.title, ju.description, ju.applicationInfo,
                        ju.address1(), ju.address2(), ju.languageCode);
            }
        }

    }

    private static MJobUpdate toJobUpdate(String jobadxml) throws Exception {

        // get the company name jobad.company.name
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(jobadxml));
        Document doc = builder.parse(is);
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        MJobUpdate ju = new MJobUpdate();
        ju.companyName = extractValue(doc, xpath, "/jobad/company/name");
        ju.companyId = MJobUpdate.companyNameToId(ju.companyName);
        ju.city = extractValue(doc, xpath, "/jobad/city");
        ju.stateabbrev = extractValue(doc, xpath, "/jobad/stateabbrev");
        ju.countryabbrev = extractValue(doc, xpath, "/jobad/countryabbrev");
        ju.postalcode = extractValue(doc, xpath, "/jobad/postalcode");
        ju.title = extractValue(doc, xpath, "/jobad/jobtitle");
        ju.description = extractValue(doc, xpath, "/jobad/jobbody");
        ju.languageCode = MJobUpdate.countryToLanguageCode(ju.countryabbrev);

        String url1 = extractValue(doc, xpath, "/jobad/combinedviewurl");
        if ( null == url1 || url1.length() == 0 ) {
            url1 = extractValue(doc, xpath, "/jobad/monsterurl");
        }

        if (null!= url1 && url1.length() > 0 ) {
            ju.applicationInfo = url1;
        }
        return ju;

    }

    private static String extractValue(Document doc, XPath xpath, String pathStr) throws Exception {
        XPathExpression expr = xpath.compile(pathStr);
        return expr.evaluate(doc, XPathConstants.STRING).toString();
    }


}