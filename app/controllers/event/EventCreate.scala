package controllers.event

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import xenlon.api.data.validation.XenlonConstraints._
import models.{ EventForm, Event, Events }
import xenlon.api.data.XenlonForms._
import scala.slick.driver.H2Driver.simple._
import play.api.Play.current

object EventCreate extends Controller {

  /** データベースコネクション */
  val database = Database.forDataSource(DB.getDataSource())

  /** イベントフォーム */
  val eventForm = Form(
    mapping(
      "eventId" -> nonEmptyText.verifying(fixLength(5)),
      "eventNm" -> nonEmptyText.verifying(maxLength(5)),
      "eventDate" -> optional(sqlDate),
      "homepage" -> optional(url))(EventForm.apply)(EventForm.unapply))

  /** 初期表示 */
  def index = Action { implicit request =>
    Ok(views.html.event.eventCreate(eventForm))
  }

  /** 登録 */
  def create =
    Action {
      implicit request =>
        database.withTransaction { implicit session =>

          eventForm.bindFromRequest.fold(
            hasErrors = { form =>
              Ok(views.html.event.eventCreate(form))
            },
            success = {
              form =>

                val event = Event(0, form.eventId, form.eventNm, form.eventDate, form.homepage)
                Events.create(event)

                Events.create(event)

                Redirect(controllers.event.routes.EventCreate.index)
                  .flashing("success" -> "登録しました。")
            })
        }
    }

  /** テーブル作成 */
  def createTable = Action { implicit request =>
    Events.createTable
    Redirect(controllers.event.routes.EventCreate.index)
      .flashing("success" -> "テーブルを作成しました")
  }

  /** テーブル削除 */
  def dropTable = Action { implicit request =>
    Events.dropTable
    Redirect(controllers.event.routes.EventCreate.index)
      .flashing("success" -> "テーブルを削除しました。")
  }
}