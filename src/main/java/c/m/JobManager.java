package c.m;


import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.cloud.talent.v4beta1.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JobManager {

    public static String[] searchJobs(String projectId, String tenantId, String optQuery, String customQ) {

        List<String> jobnames = new ArrayList<>();
        int found = 0;
        try (JobServiceClient jobServiceClient = JobServiceClient.create()) {

            String domain = "monster.com";
            String sessionId = "s1";
            String userId = "u1";

            RequestMetadata requestMetadata =
                    RequestMetadata.newBuilder()
                            .setDomain(domain)
                            .setSessionId(sessionId)
                            .setUserId(userId)
                            .build();

            TenantOrProjectName parent = TenantName.of(projectId, tenantId);
            SearchJobsRequest request = null;
            JobQuery jq = null;
            if (null != customQ && null != optQuery) {
                jq = JobQuery.newBuilder()
                        .setQuery(optQuery)
                        .setCustomAttributeFilter(customQ)
                        .build();

                request =
                        SearchJobsRequest.newBuilder()
                                .setParent(parent.toString())
                                .setJobQuery(jq)
                                .setRequestMetadata(requestMetadata)
                                .build();
                System.out.println("search for " + jq + " opt " + optQuery + " customQ " + customQ );
            } else if (null != customQ) {
                jq = JobQuery.newBuilder().setCustomAttributeFilter(customQ).build();
                request =
                        SearchJobsRequest.newBuilder()
                                .setParent(parent.toString())
                                .setJobQuery(jq)
                                .setRequestMetadata(requestMetadata)
                                .build();
                System.out.println("search for " + jq + " customQ " + customQ);
            } else if (null != optQuery){
                jq = JobQuery.newBuilder().setQuery(optQuery).build();
                request =
                        SearchJobsRequest.newBuilder()
                                .setParent(parent.toString())
                                .setJobQuery(jq)
                                .setRequestMetadata(requestMetadata)
                                .build();
                System.out.println("search for " + jq + " opt " + optQuery);

            } else {
                request =
                        SearchJobsRequest.newBuilder()
                                .setParent(parent.toString())
                                .setRequestMetadata(requestMetadata)
                                .build();
            }

            for (SearchJobsResponse.MatchingJob responseItem :
                    jobServiceClient.searchJobs(request).iterateAll()) {
                System.out.printf("== Job summary: %s\n", responseItem.getJobSummary());
                System.out.printf("== Job title snippet: %s\n", responseItem.getJobTitleSnippet());
                Job job = responseItem.getJob();
                System.out.printf("== Job name: %s\n", job.getName() + " reqid " + job.getRequisitionId());
                System.out.printf("== Job title: %s\n", job.getTitle());
                Map<String,CustomAttribute> custattr = job.getCustomAttributesMap();
                String valStr = "";
                for (String k : custattr.keySet()) {
                    for (int i = 0; i < custattr.get(k).getStringValuesCount(); i++) {
                        valStr += custattr.get(k).getStringValues(i) + " ";
                    }
                    System.out.println(" k= " + k + " = " + valStr + " str count " + custattr.get(k).getStringValuesCount());
                }
                found++;
                jobnames.add(job.getName());
            }
        } catch (Exception exception) {
            System.err.println("Failed to create the client due to: " + exception);
        }

        System.out.println("======= JobSearch found " + found);
        return jobnames.toArray(new String[0]);
    }


    public static String[] lookupJob(String projectId, String tenantId, String jId) {
        return searchJobs(projectId, tenantId, null , "mf_requisitionId=\""+jId+"\"");
    }

    public static void listJobs(String projectId, String tenantId, String filter) {
        int found = 0;
        try (JobServiceClient jobServiceClient = JobServiceClient.create()) {
            TenantOrProjectName parent = TenantName.of(projectId, tenantId);

            ListJobsRequest request =
                    ListJobsRequest.newBuilder().setParent(parent.toString()).setFilter(filter).build();
            for (Job responseItem : jobServiceClient.listJobs(request).iterateAll()) {
                System.out.printf("Job name: %s\n", responseItem.getName());
                System.out.printf("Job requisition ID: %s\n", responseItem.getRequisitionId());
                System.out.printf("Job title: %s\n", responseItem.getTitle());
                System.out.printf("Job description: %s\n", responseItem.getDescription());
                found++;
            }
        } catch (Exception exception) {
            System.err.println("Failed to create the client due to: " + exception);
        }
        System.out.println("llllllll listJobs found " + found + " for " + filter);
    }

    public static void deleteAllJobs(String projectId, String tenantId, String filter) {
        try (JobServiceClient jobServiceClient = JobServiceClient.create()) {
            TenantOrProjectName parent = TenantName.of(projectId, tenantId);

            ListJobsRequest request =
                    ListJobsRequest.newBuilder().setParent(parent.toString()).setFilter(filter).build();
            for (Job responseItem : jobServiceClient.listJobs(request).iterateAll()) {
                System.out.printf("Delete Job name: %s\n", responseItem.getName());
                System.out.printf("Job requisition ID: %s\n", responseItem.getRequisitionId());
                System.out.printf("Job title: %s\n", responseItem.getTitle());
                System.out.printf("Job description: %s\n", responseItem.getDescription());
                deleteJobByName(responseItem.getName());
            }
        } catch (Exception exception) {
            System.err.println("Failed to create the client due to: " + exception);
        }
    }

    public static void deleteJobByName(String name) {
        try (JobServiceClient jobServiceClient = JobServiceClient.create()) {
            DeleteJobRequest request = DeleteJobRequest.newBuilder().setName(name.toString()).build();
            jobServiceClient.deleteJob(request);
            System.out.println("  xx Deleted " + name);
        } catch (Exception exception) {
            System.err.println("Failed to create the client due to: " + exception);
        }
    }



    public static void makeJob(
            String projectId,
            String tenantId,
            String companyName,
            String requisitionId,
            String title,
            String description,
            String jobApplicationUrl,
            String addressOne,
            String addressTwo,
            String languageCode
    ) {

        try (JobServiceClient jobServiceClient = JobServiceClient.create()) {
            TenantOrProjectName parent = TenantName.of(projectId, tenantId);
            List<String> uris = null;
            Job.ApplicationInfo applicationInfo = null;

            if (null != jobApplicationUrl) {
                uris = Arrays.asList(jobApplicationUrl);
                applicationInfo =
                        Job.ApplicationInfo.newBuilder().addAllUris(uris).build();
            }

            List<String> addresses = Arrays.asList(addressOne, addressTwo);
            CustomAttribute reqCustA = CustomAttribute.newBuilder().setFilterable(true).addStringValues(requisitionId).build();

            Job job = null;

            if (null != applicationInfo) {
                job = Job.newBuilder()
                        .setCompany(companyName)
                        .setRequisitionId(requisitionId)
                        .setTitle(title)
                        .setDescription(description)
                        .setApplicationInfo(applicationInfo)
                        .addAllAddresses(addresses)
                        .setLanguageCode(languageCode)
                        .putCustomAttributes("mf_requisitionId", reqCustA)
                        .build();
            } else {

                job =                 Job.newBuilder()
                        .setCompany(companyName)
                        .setRequisitionId(requisitionId)
                        .setTitle(title)
                        .setDescription(description)
                        .addAllAddresses(addresses)
                        .setLanguageCode(languageCode)
                        .putCustomAttributes("mf_requisitionId", reqCustA)
                        .build();
            }

            CreateJobRequest request =
                    CreateJobRequest.newBuilder().setParent(parent.toString()).setJob(job).build();
            Job response = jobServiceClient.createJob(request);
            ++jobsCreated;

            System.out.printf("Created job: %s\n", response.getName() + " for requisitionId " + requisitionId + " jobsCreated " + jobsCreated);
        } catch (AlreadyExistsException e) {
            // do we need to delete the previous entry
            System.out.println("job exists for companyName=" + companyName
                    + " requisitionId=" + requisitionId + " languageCode=" + languageCode );
        } catch (Exception exception) {
            System.err.println("Failed to create the client due to: " + exception);
        }
    }

    private static int jobsCreated = 0;
    public static void updateJob(
            String jobName,
            String projectId,
            String tenantId,
            String companyName,
            String requisitionId,
            String title,
            String description,
            String jobApplicationUrl,
            String addressOne,
            String addressTwo,
            String languageCode) {

        deleteJobByName(jobName);
        makeJob(projectId, tenantId,
                companyName,
                requisitionId,
                title,
                description,
                jobApplicationUrl,
                addressOne,
                addressTwo,
                languageCode
        );
    }

}
