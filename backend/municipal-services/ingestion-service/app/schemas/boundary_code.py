
from pydantic import BaseModel
from typing import List, Dict, Any, Optional


class BoundaryResponse(BaseModel):
    TenantBoundary: List[Dict[str, Any]]

    @property
    def boundaries(self) -> List[Dict[str, str]]:
        """Extract block level boundaries in the required format"""
        result = []

        for tenant in self.TenantBoundary:
            # Find the ADMIN hierarchy type
            hierarchy_types = tenant.get("hierarchyType", [])
            admin_hierarchy = next((h for h in hierarchy_types if h.get("code") == "ADMIN"), None)

            if not admin_hierarchy:
                continue

            # Get the top-level boundary (country)
            boundary_data = admin_hierarchy.get("boundary", {})
            country = boundary_data.get("name", "")

            # Process state level
            for state in boundary_data.get("children", []):
                state_name = state.get("name", "")

                # Process district level
                for district in state.get("children", []):
                    district_name = district.get("name", "")

                    # Process block level
                    for block in district.get("children", []):
                        result.append({
                            "country": country,
                            "state": state_name,
                            "district": district_name,
                            "block": block.get("name", ""),
                            "code": block.get("code", "")
                        })

        return result

    def get_boundary_by_code(self, code: str) -> Optional[Dict[str, str]]:
        """Get boundary details by its code"""
        for boundary in self.boundaries:
            if boundary.get("code") == code:
                return boundary
        return None

    def get_boundaries_by_district(self, district_name: str) -> List[Dict[str, str]]:
        """Get all boundaries within a specific district"""
        return [b for b in self.boundaries if b.get("district") == district_name]

    def get_boundaries_by_state(self, state_name: str) -> List[Dict[str, str]]:
        """Get all boundaries within a specific state"""
        return [b for b in self.boundaries if b.get("state") == state_name]

    def get_states(self) -> List[str]:
        """Get list of all states"""
        return list(set(b.get("state", "") for b in self.boundaries))

    def get_districts(self, state: Optional[str] = None) -> List[str]:
        """Get list of all districts, optionally filtered by state"""
        if state:
            return list(set(b.get("district", "") for b in self.boundaries
                            if b.get("state") == state))
        return list(set(b.get("district", "") for b in self.boundaries))

    def get_blocks(self, district: Optional[str] = None) -> List[str]:
        """Get list of all blocks, optionally filtered by district"""
        if district:
            return list(set(b.get("block", "") for b in self.boundaries
                            if b.get("district") == district))
        return list(set(b.get("block", "") for b in self.boundaries))