
# Row Constraints for Ingestion Schemas

This document outlines a flexible system for expressing row-level validation rules using a custom `rowConstraints` block in ingestion schemas. These rules help enforce relationships between multiple fields that can't be captured by per-column validation alone.

---

## ✅ Types of Row Constraints

### 🔁 Presence and Dependency Constraints

#### `atLeastOneRequired`
Ensures at least one of a set of fields is non-empty.
```json
{ "type": "atLeastOneRequired", "fields": ["HFR ID", "NIN ID"] }
```

---

#### `mutuallyExclusive`
If one is present, the others must be empty.
```json
{ "type": "mutuallyExclusive", "fields": ["A", "B"] }
```

---

#### `allOrNoneRequired`
If one field is filled, all others must be filled too.
```json
{ "type": "allOrNoneRequired", "fields": ["Latitude", "Longitude"] }
```

---

#### `requires`
If `A` is present, then `B` must be too.
```json
{
  "type": "requires",
  "field": "A",
  "requiredFields": ["B"]
}
```

---

#### `requiredIfEquals`
Conditional requirements based on the value of another field.
```json
{
  "type": "requiredIfEquals",
  "field": "Type of HC",
  "value": "PHC",
  "requiredFields": ["HFR ID"]
}
```

---

### 🔎 Conditional Field Logic

#### `valueMatch`
Cross-field regex validation (rare but useful in niche cases).
```json
{
  "type": "valueMatch",
  "fieldA": "Pincode",
  "fieldB": "PoC Phone",
  "pattern": "^\d{6}0+$"
}
```

---

#### `rangeConstraint`
Use when you need to enforce a min/max numeric range.
```json
{
  "type": "rangeConstraint",
  "field": "Latitude",
  "min": -90,
  "max": 90
}
```

---

### 🧠 Semantic Validation

#### `matchLookup`
Cross-field validation against an external source (e.g., MDMS).
```json
{
  "type": "matchLookup",
  "fields": ["Vendor Code", "Vendor Type"],
  "lookup": "validVendorTypeMappings"
}
```

---

#### `customExpression`
Allows an expression engine (like CEL, JEXL, or a DSL) for advanced logic.
```json
{
  "type": "customExpression",
  "expression": "HFR_ID || NIN_ID",
  "message": "Either HFR ID or NIN ID must be present"
}
```

---

## ⚖️ Design Philosophy

- **Keep it declarative**: JSON-based and easy to read
- **Be extensible**: Add more constraint types without breaking existing ones
- **Be engine-friendly**: Each type maps cleanly to validator logic
- **Start simple**: Implement core types first (`atLeastOneRequired`, `allOrNoneRequired`, `requiredIfEquals`)

---

## 🚀 Future Enhancements

- Support expressions using a common language (CEL, JS, or safe DSL)
- Integrate with UI form validators or pre-processors
- Enable documentation auto-generation from schemas

---
