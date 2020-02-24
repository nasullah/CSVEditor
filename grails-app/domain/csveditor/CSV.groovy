package csveditor

class CSV {

    static mapping = {
        caseID type: "text"
        slideID type: "text"
        imageID type: "text"
        exportSlideID type: "text"
        anonymisedCasID type: "text"
        anonymisedImageID type: "text"
        tags type: "text"
    }

    static constraints = {
        caseID(nullable: false)
        slideID(nullable: false)
        imageID(nullable: false)
        exportSlideID(nullable: false)
        anonymisedCasID(nullable: false)
        anonymisedImageID(nullable: false)
        tags(nullable: false)
    }

    String caseID
    String slideID
    String imageID
    String exportSlideID
    String anonymisedCasID
    String anonymisedImageID
    String tags

    /*
     * Methods of the Domain Class
     */
    @Override	// Override toString for a nicer / more descriptive UI
    public String toString() {
        return "${caseID} ${slideID}";
    }
}
