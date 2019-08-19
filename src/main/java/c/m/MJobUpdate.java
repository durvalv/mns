package c.m;

public class MJobUpdate {

    public String companyName, companyId, city, stateabbrev, countryabbrev, postalcode, title, description, applicationInfo;
    public String languageCode;

    public static String companyNameToId(String companyName) {
        // normalize the company name
        return companyName.toLowerCase().trim();
    }

    public String address1() {
        return city + ", " + stateabbrev + ", " + countryabbrev;
    }

    public String address2() {
        return postalcode;
    }

    public static String countryToLanguageCode(String countryabbrev) {
        if (countryabbrev.toLowerCase().equals("us")) {
            return "en-US";
        }
        if (countryabbrev.toLowerCase().equals("nl")) {
            return "nl-NL";
        }
        return "";
    }

    public String toString() {

        String smallDesc = description;
        if (smallDesc.length() > 100)
            smallDesc = smallDesc.substring(0, 99);

        return "companyName:" + companyName + " companyid:" + companyId + " city:" + city + " state:" +  stateabbrev
                + " country:" + countryabbrev + " postalCode:" + postalcode
                + " jobTitle:" + title + " jobBody:" + smallDesc
                + " applicationInfo:" + applicationInfo + "language:" + languageCode;
    }
}
