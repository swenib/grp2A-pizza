package controllers

import play.api.mvc.{Controller, Action, AnyContent}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, number, of, text}
import forms.{CreateEmployeeForm, UpdateEmployeeForm, CreateEmployeeLogInForm, SelectEmployeeForm}
import play.api.data.format.Formats.{doubleFormat, longFormat}
import services.{EmployeeService, OrderService, UserService}

/**
  * Controller for employee specific operations.
  *
  * @author ob, scs
  */
object EmployeeController extends Controller {
  val employeeID = "Mitarbeiter ID"
  val id = "id"
  val name = "Name"
  val vorname = "Vorname"
  val gebiet = "Gebiet"
  val zugriff = "Zugriff"
  val zugriffsebene = "Zugriffsebene"
  val stundenrate = "Stundenrate"
  val eMail = "E-Mail"
  val passwort = "Passwort"
  val success = "success"
  val employeeSaved = "Employee Saved!"
  val loggedInEmployee = "loggedInEmployee"

  /**
    * Form object for employee data.
    */

  val employeeForm = Form(
    mapping(
      name -> nonEmptyText,
      vorname -> nonEmptyText,
      gebiet -> nonEmptyText,
      zugriff -> nonEmptyText,
      zugriffsebene -> number,
      stundenrate -> of(doubleFormat),
      eMail -> nonEmptyText,
      passwort -> nonEmptyText)(CreateEmployeeForm.apply)(CreateEmployeeForm.unapply))


  /**
    * Form object for update employee data.
    */
  val updateEmployeeForm = Form(
    mapping(
      id -> of(longFormat),
      name -> nonEmptyText,
      vorname -> nonEmptyText,
      gebiet -> nonEmptyText,
      zugriff -> nonEmptyText,
      zugriffsebene -> number,
      stundenrate -> of(doubleFormat),
      eMail -> nonEmptyText,
      passwort -> nonEmptyText)(UpdateEmployeeForm.apply)(UpdateEmployeeForm.unapply))


  /**
    * Form object for employee login.
    */
  val employeeLogInForm = Form(
    mapping(
      eMail -> nonEmptyText,
      passwort -> nonEmptyText)(CreateEmployeeLogInForm.apply)(CreateEmployeeLogInForm.unapply))


  /**
    * Form object to select an employee.
    */
  val selectEmployeeForm = Form(
    mapping(
      employeeID -> of(longFormat))(SelectEmployeeForm.apply)(SelectEmployeeForm.unapply))

  /**
    * Shows the view to register a new employee.
    */
  def addEmployee: Action[AnyContent] = Action { implicit request =>
    employeeForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.badRequest())
      },
      employeeData => {
        val newEmployee = services.EmployeeService.addEmployee(employeeData.name,
          employeeData.lastname, employeeData.workplace, employeeData.acces,
          employeeData.accesLevel, employeeData.netRate, employeeData.email,
          employeeData.password)
        Redirect(routes.EmployeeController.newEmployeeCreated(newEmployee.name,
          newEmployee.lastname, newEmployee.workplace, newEmployee.acces,
          newEmployee.accesLevel, newEmployee.netRate, newEmployee.email,
          newEmployee.password)).flashing(success -> employeeSaved)
      })
  }

  /**
    * Shows the view to update an employee.
    */
  def updateEmployee: Action[AnyContent] = Action { implicit request =>
    updateEmployeeForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.badRequest())
      },
      updateEmployeeData => {
        val selectEmployee = services.EmployeeService.updateEmployee(updateEmployeeData.id,
          updateEmployeeData.name, updateEmployeeData.lastname, updateEmployeeData.workplace,
          updateEmployeeData.acces, updateEmployeeData.accesLevel, updateEmployeeData.netRate,
          updateEmployeeData.email, updateEmployeeData.password)
        Redirect(routes.EmployeeController.upgradeEmployee(selectEmployee.id, selectEmployee.name,
          selectEmployee.lastname, selectEmployee.workplace, selectEmployee.acces,
          selectEmployee.accesLevel, selectEmployee.netRate, selectEmployee.email,
          selectEmployee.password)).flashing(success -> employeeSaved)
      })
  }

  /**
    * Shows the view to choose an employee.
    */
  def chooseEmployee: Action[AnyContent] = Action { implicit request =>
    selectEmployeeForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.badRequest())
      },
      selectEmployeeData => {
        val selectEmployee = services.EmployeeService.getEmployeeByID(selectEmployeeData.id)
        Redirect(routes.EmployeeController.changeEmployee(selectEmployee.id, selectEmployee.name,
          selectEmployee.lastname, selectEmployee.workplace, selectEmployee.acces,
          selectEmployee.accesLevel, selectEmployee.netRate, selectEmployee.email,
          selectEmployee.password, selectEmployee.activeFlag)).
          flashing(success -> employeeSaved)
      })
  }

  /**
    * Shows the view for loged in employees.
    */
  def logInEmployee: Action[AnyContent] = Action { implicit request =>
    employeeLogInForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.employeeLogIn(employeeForm, EmployeeService.registredEmployees,
          formWithErrors))
      },
      employeeData => {
        try {
          val newEmployee = services.EmployeeService.logInEmployee(employeeData.email,
            employeeData.password)
          Redirect(routes.EmployeeController.completeLogInEmployee(newEmployee.id,
            newEmployee.name)).flashing(success -> employeeSaved)
        } catch {
          case e: RuntimeException => BadRequest(views.html.loginFailed())
        }
      }
    )
  }

  /**
    * Shows the view for an inaktiv employee.
    */
  def employeeFlagZero: Action[AnyContent] = Action { implicit request =>
    selectEmployeeForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.badRequest())
      },
      updateEmployeeData => {
        val selectEmployee = services.EmployeeService.setEmployeeFlag0(updateEmployeeData.id)
        Redirect(routes.EmployeeController.setEmployeeFlag(selectEmployee)).
          flashing(success -> employeeSaved)
      })
  }

  /**
    * Shows the view for an aktiv employee.
    */
  def employeeFlagOne: Action[AnyContent] = Action { implicit request =>
    selectEmployeeForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.badRequest())
      },
      updateEmployeeData => {
        val selectEmployee = services.EmployeeService.setEmployeeFlag1(updateEmployeeData.id)
        Redirect(routes.EmployeeController.setEmployeeFlag(selectEmployee)).
          flashing(success -> employeeSaved)
      })
  }


  /**
    * Shows the view for an successfully deleted employee.
    */
  def rmEmployee: Action[AnyContent] = Action { implicit request =>
    selectEmployeeForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.badRequest())
      },
      deleteEmployeeData => {
        val deleteEmployeeVal = services.EmployeeService.rmEmployee(deleteEmployeeData.id)
        Redirect(routes.EmployeeController.employeeDeleted(deleteEmployeeVal)).
          flashing(success -> "Extra saved!")
      })
  }

  /**
    * Shows the welcome view for a newly registered employee.
    */

  def registerEmployee = Action { request =>
    request.session.get(loggedInEmployee).map { userID =>
      Ok(views.html.employeeLoggedIn(userID, EmployeeService.getEmployeeByID(userID.toLong).name,
        controllers.EmployeeController.employeeForm,
        controllers.EmployeeController.selectEmployeeForm,
        EmployeeService.registredEmployees, controllers.UserController.userForm,
        UserService.registeredUsers, EmployeeService.getEmployeeByID(userID.toLong).accesLevel,
        EmployeeService.getEmployeeByID(userID.toLong).activeFlag,
        EmployeeService.getEmployeeByID(userID.toLong).acces, PizzaController.pizzaForm))
    }.getOrElse {
      Ok(views.html.employeeLogIn(controllers.EmployeeController.employeeForm,
        EmployeeService.registredEmployees,
        controllers.EmployeeController.employeeLogInForm))
    }
  }

  /**
    * Shows the view for logedin employees.
    */
  def completeLogInEmployee(id: Long, name: String): Action[AnyContent] = Action {
    Ok(views.html.employeeLoggedIn(id.toString, name, controllers.EmployeeController.employeeForm,
      controllers.EmployeeController.selectEmployeeForm, EmployeeService.registredEmployees,
      controllers.UserController.userForm,
      UserService.registeredUsers,
      EmployeeService.getEmployeeByID(id).accesLevel,
      EmployeeService.getEmployeeByID(id).activeFlag,
      EmployeeService.getEmployeeByID(id.toLong).acces,
      PizzaController.pizzaForm)).withSession(
      loggedInEmployee -> id.toString
    )
  }

  /**
    * Shows the view for an new created employee.
    */
  def newEmployeeCreated(name: String, lastname: String, workplace: String, acces: String,
                         accesLevel: Int, netRate: Double, email: String,
                         password: String): Action[AnyContent] = Action {
    Ok(views.html.newEmployeeCreated(name, lastname, workplace, acces, accesLevel,
      netRate, email, password))
  }

  /**
    * Shows the view to edit an employee.
    */
  def changeEmployee(id: Long, name: String, lastname: String, workplace: String, acces: String,
                     accesLevel: Int, netRate: Double, email: String,
                     password: String, activeFlag: Int): Action[AnyContent] = Action {
    Ok(views.html.changeEmployee(id, name, lastname, workplace, acces,
      accesLevel, netRate, email, password,
      activeFlag, controllers.EmployeeController.updateEmployeeForm))
  }

  /**
    * Shows the view to edit an employee.
    */
  def upgradeEmployee(id: Long, name: String, lastname: String, workplace: String, acces: String,
                      accesLevel: Int, netRate: Double, email: String,
                      password: String): Action[AnyContent] = Action {
    Ok(views.html.employeeUpdated(id, name, lastname, workplace, acces,
      accesLevel, netRate, email, password))
  }

  /**
    * Shows the view to delete an employee.
    */
  def employeeDeleted(deleted: Boolean): Action[AnyContent] = Action {
    Ok(views.html.employeeDeleted())
  }

  /**
    * Shows the view to set an employee active/inactive.
    */
  def setEmployeeFlag(id: Double): Action[AnyContent] = Action {
    Ok(views.html.employeeFlagChanged(id))
  }


  /**
    * Shows the welcome view for a newly registered employee.
    */

  def showEmployees: Action[AnyContent] = Action {
    Ok(views.html.allEmployees(EmployeeService.registredEmployees))
  }

  /**
    * Shows the view for a all Order details.
    */
  def showAllOrderDetails: Action[AnyContent] = Action {
    Ok(views.html.allOrders(OrderService.availableOrderWithAdress))
  }
}
