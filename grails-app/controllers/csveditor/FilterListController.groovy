package csveditor

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class FilterListController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond FilterList.list(params), model:[filterListCount: FilterList.count()]
    }

    def show(FilterList filterList) {
        respond filterList
    }

    def create() {
        respond new FilterList(params)
    }

    def uploadFile = {
        if (!request.getFile('file').originalFilename) {
            flash.message = "Please choose a file"
            redirect action:"index", method:"GET"
        } else{
            request.getFile('file').inputStream.splitEachLine(',')
                    { fields ->
                        def caseID = fields[0].trim()
                        if (caseID){
                            def row = new FilterList()
                            row.caseID = caseID
                            row.save flush: true
                        }
                    }
            flash.message = "records created"
            redirect action:"index", method:"GET"
        }
    }

    @Transactional
    def save(FilterList filterList) {
        if (filterList == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (filterList.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond filterList.errors, view:'create'
            return
        }

        filterList.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'filterList.label', default: 'FilterList'), filterList.id])
                redirect filterList
            }
            '*' { respond filterList, [status: CREATED] }
        }
    }

    def edit(FilterList filterList) {
        respond filterList
    }

    @Transactional
    def update(FilterList filterList) {
        if (filterList == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (filterList.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond filterList.errors, view:'edit'
            return
        }

        filterList.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'filterList.label', default: 'FilterList'), filterList.id])
                redirect filterList
            }
            '*'{ respond filterList, [status: OK] }
        }
    }

    @Transactional
    def delete(FilterList filterList) {

        if (filterList == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        filterList.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'filterList.label', default: 'FilterList'), filterList.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'filterList.label', default: 'FilterList'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
