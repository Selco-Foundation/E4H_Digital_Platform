Map<String, dynamic> assessmentMobileSchema(String code) => {
      'schemaCode': 'assessment.AssessmentMobileFormSchema',
      'uniqueIdentifier': code,
      'isActive': true,
      'data': {
        'name': 'Assessment.$code',
        'formType': code,
        'pages': [
          {
            'page': 'details',
            'properties': [
              {'fieldName': 'remarks', 'type': 'string', 'format': 'text'}
            ]
          }
        ]
      },
    };
