package c.m;

import com.google.cloud.talent.v4beta1.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CompanyManager {
    private static HashMap<String,Company> companiesMap = new HashMap<>();

    public static String lookupCompany(String projectId, String tenantId, String companyName) {

        if (companiesMap.containsKey(companyName)) {
            // cache of previous companies
            return companiesMap.get(companyName).getName();
        }

        listCompanies(projectId, tenantId);
        if (! companiesMap.containsKey(companyName)) {
            return "";
        }

        return companiesMap.get(companyName).getName();
    }

    public static synchronized void listCompanies(String projectId, String tenantId) {

            try (CompanyServiceClient companyServiceClient = CompanyServiceClient.create()) {
                TenantOrProjectName parent = TenantName.of(projectId, tenantId);
                ListCompaniesRequest request =
                        ListCompaniesRequest.newBuilder().setParent(parent.toString()).build();
                for (Company responseItem : companyServiceClient.listCompanies(request).iterateAll()) {
                    System.out.printf("Company Name: %s\n", responseItem.getName());
                    System.out.printf("Display Name: %s\n", responseItem.getDisplayName());
                    System.out.printf("External ID: %s\n", responseItem.getExternalId());
                    companiesMap.put(responseItem.getDisplayName(), responseItem);
                }
            } catch (Exception exception) {
                System.err.println("Failed to create the client due to: " + exception);
            }
            System.out.println("  ========= number of cached companies " + companiesMap.size());
    }

    private static int companiesCreated = 0;
    public static String createCompany(
            String projectId, String tenantId, String displayName, String externalId) {
        try (CompanyServiceClient companyServiceClient = CompanyServiceClient.create()) {
            TenantOrProjectName parent = TenantName.of(projectId, tenantId);
            Company company =
                    Company.newBuilder().setDisplayName(displayName).setExternalId(externalId).build();
            CreateCompanyRequest request =
                    CreateCompanyRequest.newBuilder()
                            .setParent(parent.toString())
                            .setCompany(company)
                            .build();
            Company response = companyServiceClient.createCompany(request);
            ++companiesCreated;
            System.out.println("Companies Created " + companiesCreated);
            System.out.printf("Name: %s\n", response.getName());
            System.out.printf("Display Name: %s\n", response.getDisplayName());
            System.out.printf("External ID: %s\n", response.getExternalId());
            return response.getName();
        } catch (Exception exception) {
            System.err.println("Failed to create the client due to: " + exception);
        }
        return "";
    }


}
