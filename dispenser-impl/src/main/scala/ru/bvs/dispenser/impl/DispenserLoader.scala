package ru.bvs.dispenser.impl

import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import ru.bvs.dispenser.api.DispenserService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.softwaremill.macwire._

class DispenserLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new DispenserApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new DispenserApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[DispenserService])
}

abstract class DispenserApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
        with CassandraPersistenceComponents
        with LagomKafkaComponents
        with AhcWSComponents {

  override lazy val lagomServer: LagomServer = serverFor[DispenserService](wire[DispenserServiceImpl])

  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = DispenserSerializerRegistry

  clusterSharding.init(
    Entity(DispenserState.typeKey)(
      entityContext => DispenserBehavior.create(entityContext)
    )
  )

}
