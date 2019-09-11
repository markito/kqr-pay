//
// kamel run --dev payment-service.groovy
//

from('knative:endpoint/payment-service')
    .setBody(constant('in stock'))
    .log('${body}')

