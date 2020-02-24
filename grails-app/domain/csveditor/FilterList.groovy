package csveditor

class FilterList {

    static mapping = {
        caseID type: "text"
    }

    static constraints = {
        caseID(nullable: false)
    }

    String caseID

    /*
     * Methods of the Domain Class
     */
    @Override	// Override toString for a nicer / more descriptive UI
    public String toString() {
        return "${caseID}";
    }
}
