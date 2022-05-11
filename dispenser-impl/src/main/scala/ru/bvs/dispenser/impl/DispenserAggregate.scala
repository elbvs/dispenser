package ru.bvs.dispenser.impl

import play.api.libs.json.Json
import play.api.libs.json.Format
import java.time.LocalDateTime
import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl._
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import com.lightbend.lagom.scaladsl.persistence.AggregateEvent
import com.lightbend.lagom.scaladsl.persistence.AggregateEventTag
import com.lightbend.lagom.scaladsl.persistence.AkkaTaggerAdapter
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry

// объект для создания поведения
object DispenserBehavior {

    def create(entityContext: EntityContext[DispenserCommand]): Behavior[DispenserCommand] = {
        val persistenceId: PersistenceId = PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId)

        create(persistenceId)
            .withTagger(
                AkkaTaggerAdapter.fromLagom(entityContext, DispenserEvent.Tag)
            )

    }

    private[impl] def create(persistenceId: PersistenceId) = EventSourcedBehavior
        .withEnforcedReplies[DispenserCommand, DispenserEvent, DispenserState](
            persistenceId = persistenceId,
            emptyState = DispenserState.initial,
            commandHandler = (state, cmd) => state.applyCommand(cmd),
            eventHandler = (state, evt) => state.applyEvent(evt)
        )
}

// Класс описывающий актор состояния
case class DispenserState(timestamp: String) {
    def applyCommand(cmd: DispenserCommand): ReplyEffect[DispenserEvent, DispenserState] =
        cmd match {
            case _ => Effect.noReply
        }

    def applyEvent(evt: DispenserEvent): DispenserState =
        evt match {
            case _ => updateState()
        }

    private def updateState() = copy(LocalDateTime.now().toString)
}

object DispenserState {

    // начальное состояние
    def initial: DispenserState = DispenserState(LocalDateTime.now.toString)

    // ключ для сущности
    val typeKey: EntityTypeKey[DispenserCommand] = EntityTypeKey[DispenserCommand]("DispenserAggregate")

    implicit val format: Format[DispenserState] = Json.format
}

sealed trait DispenserEvent extends AggregateEvent[DispenserEvent] {
    def aggregateTag: AggregateEventTag[DispenserEvent] = DispenserEvent.Tag
}

object DispenserEvent {
    val Tag: AggregateEventTag[DispenserEvent] = AggregateEventTag[DispenserEvent]
}

trait DispenserCommandSerializable

sealed trait DispenserCommand
    extends DispenserCommandSerializable

object DispenserSerializerRegistry extends JsonSerializerRegistry {
    override def serializers: Seq[JsonSerializer[_]] = Seq.empty[JsonSerializer[_]]
}
