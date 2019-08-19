package c.m.gsearcher;

import c.m.CompanyManager;
import c.m.GctsClientTool;

public class ListCompanies {
    public static void main(String[] a) throws Exception {
        CompanyManager.listCompanies(GctsClientTool.getProjectId(), GctsClientTool.getTenantId());
    }
}
