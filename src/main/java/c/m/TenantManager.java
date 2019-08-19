package c.m;

import com.google.cloud.talent.v4beta1.*;

import java.util.ArrayList;
import java.util.List;

public class TenantManager {

    public static synchronized String lookupTenant(String projectId, String externalId) {

        if (null == tenants) {
            listTenants(projectId);
        }
        for (Tenant ten : tenants) {
            if (ten.getExternalId().equals(externalId)) {
                String tname = ten.getName();
                System.out.println("found " + tname + " for " + externalId);
                return exId(tname);
            }
        }
        return "";
    }

    private static String exId(String tname) {
        String [] parts = tname.split("/");
        if (parts.length>0) {
            tname = parts[parts.length-1];
        }
        return tname;
    }

    public static String createTenant(String projectId, String tenantUniqueName) {
        try (TenantServiceClient tenantServiceClient = TenantServiceClient.create()) {
            ProjectName parent = ProjectName.of(projectId);
            Tenant tenant = Tenant.newBuilder().setExternalId(tenantUniqueName).build();
            CreateTenantRequest request =
                    CreateTenantRequest.newBuilder().setParent(parent.toString()).setTenant(tenant).build();
            Tenant response = tenantServiceClient.createTenant(request);
            System.out.println("Created Tenant");
            System.out.printf("Name: %s\n", response.getName());
            System.out.printf("External ID: %s\n", response.getExternalId());
            return  exId(response.getName());
        } catch (Exception exception) {
            System.err.println("Failed to create the client due to: " + exception);
        }

        return "";
    }

    private static List<Tenant> tenants = null;
    public static synchronized void listTenants(String projectId) {

        if (null == tenants) {
            tenants = new ArrayList<>();

            try (TenantServiceClient tenantServiceClient = TenantServiceClient.create()) {
                ProjectName parent = ProjectName.of(projectId);
                ListTenantsRequest request =
                        ListTenantsRequest.newBuilder().setParent(parent.toString()).build();
                for (Tenant responseItem : tenantServiceClient.listTenants(request).iterateAll()) {
                    tenants.add(responseItem);
                }
            } catch (Exception exception) {
                System.err.println("Failed to create the client due to: " + exception);
            }

            for (Tenant responseItem : tenants) {
                System.out.printf("Tenant Name: %s\n", responseItem.getName());
                System.out.printf("External ID: %s\n", responseItem.getExternalId());
            }

        }
    }

    public static void deleteTenant(String projectId, String tenantId) {
        try (TenantServiceClient tenantServiceClient = TenantServiceClient.create()) {
            TenantName name = TenantName.of(projectId, tenantId);
            DeleteTenantRequest request =
                    DeleteTenantRequest.newBuilder().setName(name.toString()).build();
            tenantServiceClient.deleteTenant(request);
            System.out.println("  tt Deleted Tenant " + tenantId);
        } catch (Exception exception) {
            System.err.println("Failed to create the client due to: " + exception);
        }
    }
}
