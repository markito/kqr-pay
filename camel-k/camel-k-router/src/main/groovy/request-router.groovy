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
    return it.containsKey('a')
} as java.util.function.Function<Object, Object>

from('knative:endpoint/camel-router')
    .unmarshal()
        .json(JsonLibrary.Jackson, Map.class)
        .log('${body}')
    .choice()
        .when().body(itemIsInStock)
            .marshal()
                .json(JsonLibrary.Jackson)
            .to('knative:endpoint/payment-service')
        .otherwise()
            .transform().body(createOrder)
            .to('salesforce:createSObject')
            .transform(simple('${body.id}'))
            .to('salesforce:getSObject?sObjectName=Order&rawPayload=true')
            .unmarshal()
                .json(JsonLibrary.Jackson, Map.class)
            .setBody(simple('Order number: ${body[OrderNumber]}'))
    .end()
    .log('${body}')

