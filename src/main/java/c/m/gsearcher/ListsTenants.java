package c.m.gsearcher;

import c.m.GctsClientTool;
import c.m.TenantManager;

public class ListsTenants {
    public static void main(String[] a) {
        TenantManager.listTenants(GctsClientTool.getProjectId());
    }
}
