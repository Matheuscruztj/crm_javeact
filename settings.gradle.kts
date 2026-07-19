rootProject.name = "atlasops-ai"

include(
    "backend:shared-kernel",
    "backend:auth",
    "backend:tenants",
    "backend:users",
    "backend:customers",
    "backend:documents",
    "backend:requests",
    "backend:approvals",
    "backend:activities",
    "backend:notifications",
    "backend:integrations",
    "backend:search",
    "backend:imports",
    "backend:operations",
    "backend:ai",
    "backend:analytics",
    "backend:audit",
    "backend:app-boot",
    "backend:worker"
)
