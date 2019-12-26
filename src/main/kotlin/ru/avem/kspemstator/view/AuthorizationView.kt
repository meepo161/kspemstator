package ru.avem.kspemstator.view

import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.database.entities.User
import ru.avem.kspemstator.database.entities.Users
import ru.avem.kspemstator.database.entities.Users.login
import tornadofx.*
import tornadofx.controlsfx.confirmNotification
import tornadofx.controlsfx.warningNotification

class AuthorizationView : View("Авторизация") {
    private val loginProperty = SimpleStringProperty("")
    private val passwordProperty = SimpleStringProperty("")

    lateinit var users: List<User>

    override val root = anchorpane {
        prefWidth = 1200.0
        prefHeight = 700.0

        vbox(spacing = 16.0) {
            anchorpaneConstraints {
                topAnchor = 0.0
                bottomAnchor = 0.0
                leftAnchor = 0.0
                rightAnchor = 0.0
            }

            alignmentProperty().set(Pos.CENTER)

            label("Авторизация") {}.style = "-fx-font-size: 25 px"

            hbox(spacing = 24.0) {
                alignmentProperty().set(Pos.CENTER)

                label("Логин") {
                }.addClass(Styles.medium)

                textfield {
                    promptText = "Логин"

                    addClass(Styles.medium)
                }.bind(loginProperty)
            }

            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER)

                label("Пароль") {
                }.addClass(Styles.medium)

                passwordfield {
                    promptText = "Пароль"
                    addClass(Styles.medium)
                }.bind(passwordProperty)
            }

            button("Вход") {
                anchorpaneConstraints {
                    leftAnchor = 16.0
                    rightAnchor = 16.0
                    topAnchor = 270.0
                }

                onAction = EventHandler {
                    if (loginProperty.value.isEmpty() || passwordProperty.value.isEmpty()) {
                        warningNotification(
                            "Пустой логин или пароль",
                            "Заполните все поля",
                            Pos.BOTTOM_CENTER,
                            hideAfter = 3.seconds
                        )
                        return@EventHandler
                    }
                    transaction {
                        users = User.find {
                            (login eq loginProperty.value) and (Users.password eq passwordProperty.value)
                        }.toList()
                        if (users.isEmpty()) {
                            warningNotification(
                                "Неправильный логин или пароль", "Проверьте данные для входа и повторите снова.",
                                Pos.BOTTOM_CENTER, hideAfter = 3.seconds
                            )
                        } else {
                            confirmNotification(
                                "Авторизация",
                                "Вы вошли как: ${loginProperty.value}",
                                Pos.BOTTOM_CENTER,
                                hideAfter = 3.seconds
                            )
                            replaceWith<MainView>(
                                ViewTransition.Metro(1.0.seconds),
                                sizeToScene = true,
                                centerOnScreen = true
                            )
                        }
                    }
                }
            }.style = "-fx-font-size: 14 px"
        }
    }
}
