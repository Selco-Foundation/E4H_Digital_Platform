# TODO

- [ ] Better String Validations, i.e. allow more characters, numbers, commas, etc
- [ ] Read Boundary Codes to fetch facility selection template
- [ ] Vendor Creation shows success even when API fails. Check for all ingestions.
- [ ] facility selection template: facility search by boundary should be able to drill down from Country Level boundary code, currently expects block level boundary code
- [ ] facility ingestion is failing silently, the service succeeds in sendng a message to the persister topic but persister has no logs of insert or error.
