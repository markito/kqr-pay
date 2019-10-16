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
    return it.get('productIds').contains('v')
} as java.util.function.Function<Object, Object>

from('knative:endpoint/camel-router')
    .log('\n\nreceived order : ${body}\n')
    .setProperty('jsonbody', simple('${body}'))
    .to('knative:endpoint/payment-service')
    .log('\npayment processed : ${body}\n')
    /*
    .setBody(simple('${exchangeProperty.jsonbody}'))
    .unmarshal().json(JsonLibrary.Jackson, Map.class)
    .choice()
        .when().body(itemIsInStock)
            .log('\nPlacing order for virtual items in Salesforce\n')
            .transform().body(createOrder)
            .to('salesforce:createSObject')
            .transform(simple('${body.id}'))
            .to('salesforce:getSObject?sObjectName=Order&rawPayload=true')
            .unmarshal()
                .json(JsonLibrary.Jackson, Map.class)
            .log('\nSalesforce order created ${body[OrderNumber]}\n')
            .setBody(simple('Order number: ${body[OrderNumber]}'))
    .end()
    */

