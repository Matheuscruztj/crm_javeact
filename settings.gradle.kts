rootProject.name = "atlasops-ai"

include(
    "backend:shared-kernel",
    "backend:auth",
    "backend:tenants",
    "backend:users",
    "backend:customers",
    "backend:documents",
    "backend:requests",
    "backend:pipeline",
    "backend:tasks",
    "backend:workflows",
    "backend:ai",
    "backend:analytics",
    "backend:audit",
    "backend:app-boot"
)
