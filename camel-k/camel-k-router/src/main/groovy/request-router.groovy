//
// kamel run --dev request-router.groovy --dependency=mvn:com.github.lburgazzoli/camel-k-kqr-pay-support/1.0-SNAPSHOT --secret=salesforce
//

import com.github.lburgazzoli.camel.salesforce.model.Order
import com.github.lburgazzoli.camel.salesforce.model.Order_StatusEnum
import org.apache.camel.model.dataformat.JsonLibrary

import java.time.LocalDate

def createOrder = {
    Order order = new Order()
    order.accountId = '0011R00002Io2wM'
    order.contractId = '8001R000007mkoW'
    order.effectiveDate = LocalDate.now()
    order.status = Order_StatusEnum.DRAFT
    return order
} as java.util.function.Function<Object, Object>

def itemIsInStock = {
    return it.containsKey('virtual')
} as java.util.function.Function<Object, Object>

from('knative:endpoint/camel-router')
    .setHeader('jsonbody', simple('${body}'))
    .to('knative:endpoint/payment-service')
    .setBody(simple('${header.jsonbody}'))
    .unmarshal().json(JsonLibrary.Jackson, Map.class)
    .choice()
        .when().body(itemIsInStock)
            .log('calling salesforce')
            .transform().body(createOrder)
            .to('salesforce:createSObject')
            .transform(simple('${body.id}'))
            .to('salesforce:getSObject?sObjectName=Order&rawPayload=true')
            .unmarshal()
                .json(JsonLibrary.Jackson, Map.class)
            .setBody(simple('Order number: ${body[OrderNumber]}'))
        .otherwise()
            .log('no virtual items')
    .end()
    .log('${body}')

