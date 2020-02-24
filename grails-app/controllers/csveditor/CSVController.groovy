package csveditor

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class CSVController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond CSV.list(params), model:[CSVCount: CSV.count()]
    }

    def show(CSV CSV) {
        respond CSV
    }

    def create() {
        respond new CSV(params)
    }

    def uploadFile = {
        if (!request.getFile('file').originalFilename) {
            flash.message = "Please choose a file"
            redirect action:"index", method:"GET"
        } else{
            request.getFile('file').inputStream.splitEachLine(',')
                    { fields ->
                        def caseID = fields[0].trim()
                        def slideID = fields[1].trim()
                        def imageID = fields[2].trim()
                        def generatedId = fields[1].toString().trim()
                        def parts = generatedId.split('_')
                        if (parts.size() == 3){
                            if(!CSV.findAllBySlideID(slideID)?.empty && CSV.findAllBySlideID(slideID)?.size() == 1){
                                generatedId =  parts[0] + '_' + parts[1] + '_' + parts[2].substring(0, 1) + '_' +  parts[2].substring(parts[2].length() -2) + '_1'
                            }else if(!CSV.findAllBySlideID(slideID)?.empty && CSV.findAllBySlideID(slideID)?.size() == 2){
                                generatedId =  parts[0] + '_' + parts[1] + '_' + parts[2].substring(0, 1) + '_' +  parts[2].substring(parts[2].length() -2) + '_2'
                            }else if(CSV.findAllBySlideID(slideID)?.empty){
                                generatedId =  parts[0] + '_' + parts[1] + '_' + parts[2].substring(0, 1) + '_' +  parts[2].substring(parts[2].length() -2)
                            }
                        }
                        def exportSlideID = "export\\" + generatedId + ".isyntax"
                        def anonymisedCasID = fields[4].trim()
                        def anonymisedImageID = fields[5].trim()
                        def tags = fields[6].trim()
                        if (caseID){
                            def row = new CSV()
                            row.caseID = caseID
                            row.slideID = slideID
                            row.imageID = imageID
                            row.exportSlideID = exportSlideID
                            row.anonymisedCasID = anonymisedCasID
                            row.anonymisedImageID = anonymisedImageID
                            row.tags =tags
                            row.save flush: true
                        }
                    }
            flash.message = "records created"
            redirect action:"index", method:"GET"
        }
    }

    def exportService
    def exportFilteredCSV(){
        if(params?.extension && params?.extension != "html"){
            response.contentType = grailsApplication.config.grails.mime.types[params?.extension]
            response.setHeader("Content-disposition", "attachment; filename= filteredCSV.${params.extension}")
            def rows = []
            FilterList.list().each {
                c->
                    def row = CSV.findAllByCaseID(c.caseID)
                    rows.addAll(row)
            }
            List fields = ["caseID", "slideID", "imageID", "exportSlideID", "anonymisedCasID", "anonymisedImageID", "tags"]
            Map labels = ["caseID":"Case ID", "slideID":"Slide ID", "imageID":"Image ID", "exportSlideID":"export\\Slide ID.isyntax",
                          "anonymisedCasID":"Anonymized Case ID", "anonymisedImageID":"Anonymized Image ID", "tags":"Tags"]
            Map formatters = ["quoteCharacter": "\u0000"]
            Map parameters = [title: "Annotation"]
            exportService.export(params.extension, response.outputStream, rows, fields, labels, formatters, parameters )
        }
    }

    @Transactional
    def save(CSV CSV) {
        if (CSV == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (CSV.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond CSV.errors, view:'create'
            return
        }

        CSV.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'CSV.label', default: 'CSV'), CSV.id])
                redirect CSV
            }
            '*' { respond CSV, [status: CREATED] }
        }
    }

    def edit(CSV CSV) {
        respond CSV
    }

    @Transactional
    def update(CSV CSV) {
        if (CSV == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (CSV.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond CSV.errors, view:'edit'
            return
        }

        CSV.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'CSV.label', default: 'CSV'), CSV.id])
                redirect CSV
            }
            '*'{ respond CSV, [status: OK] }
        }
    }

    @Transactional
    def delete(CSV CSV) {

        if (CSV == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        CSV.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'CSV.label', default: 'CSV'), CSV.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'CSV.label', default: 'CSV'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
