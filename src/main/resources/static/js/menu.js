document.addEventListener('wheel', function(event) {
    if (document.activeElement.type === 'number') {
        document.activeElement.blur();
    }
}, { passive: false });

document.addEventListener("DOMContentLoaded", function () {
    const currentPath = window.location.pathname;
    const links = document.querySelectorAll(".menu-link");
    const body = document.body;
    const sidebarToggle = document.getElementById("sidebarToggle");
    const mobileSidebarToggle = document.getElementById("mobileSidebarToggle");
    const sidebarToggleIcon = document.getElementById("sidebarToggleIcon");
    const collapsedKey = "sidebar-collapsed";
    const mobileMedia = window.matchMedia("(max-width: 1199.98px)");

    links.forEach(link => {
        const linkPath = link.getAttribute("href");

        if (linkPath && currentPath.startsWith(linkPath)) {
            link.classList.add("active");

            const collapse = link.closest(".collapse");
            if (collapse) {
                collapse.classList.add("show");

                const parent = collapse.closest(".user-menu");
                const arrow = parent?.querySelector(".arrow-icon");
                if (arrow) {
                    arrow.classList.add("rotate");
                }
            }

            const parent = link.closest(".user-menu");
            const parentLink = parent?.querySelector(".menu-parent");
            if (parentLink) {
                parentLink.classList.add("active");
            }
        }
    });

    document.querySelectorAll(".menu-parent").forEach(parent => {
        parent.addEventListener("click", function (event) {
            const userMenu = this.closest(".user-menu");

            if (body.classList.contains("sidebar-collapsed")) {
                event.preventDefault();
                document.querySelectorAll(".user-menu").forEach(menu => {
                    if (menu !== userMenu) {
                        menu.classList.remove("submenu-open");
                    }
                });
                userMenu?.classList.toggle("submenu-open");
                return;
            }

            const arrow = this.querySelector(".arrow-icon");
            if (arrow) {
                arrow.classList.toggle("rotate");
            }
        });
    });

    function setCollapsed(collapsed) {
        body.classList.toggle("sidebar-collapsed", collapsed);
        if (mobileMedia.matches) {
            document.getElementById("appSidebar")?.classList.toggle("is-open", !collapsed);
        }
        if (!collapsed) {
            document.querySelectorAll(".user-menu").forEach(menu => {
                menu.classList.remove("submenu-open");
            });
        }
        if (sidebarToggleIcon) {
            sidebarToggleIcon.textContent = collapsed ? ">" : "<";
        }
        localStorage.setItem(collapsedKey, collapsed ? "true" : "false");
    }

    function applyResponsiveSidebar() {
        const isMobile = mobileMedia.matches;
        body.classList.toggle("sidebar-mobile", isMobile);

        if (isMobile) {
            setCollapsed(true);
            return;
        }

        const savedCollapsed = localStorage.getItem(collapsedKey) === "true";
        setCollapsed(savedCollapsed);
    }

    if (sidebarToggle) {
        applyResponsiveSidebar();

        sidebarToggle.addEventListener("click", function () {
            setCollapsed(!body.classList.contains("sidebar-collapsed"));
        });
    }

    if (mobileSidebarToggle) {
        mobileSidebarToggle.addEventListener("click", function () {
            setCollapsed(!body.classList.contains("sidebar-collapsed"));
        });
    }

    if (typeof mobileMedia.addEventListener === "function") {
        mobileMedia.addEventListener("change", applyResponsiveSidebar);
    } else if (typeof mobileMedia.addListener === "function") {
        mobileMedia.addListener(applyResponsiveSidebar);
    }

    document.addEventListener("click", function (event) {
        if (!body.classList.contains("sidebar-collapsed")) {
            return;
        }

        const insideSidebarMenu = event.target.closest(".user-menu") || event.target.closest("#sidebarToggle");
        if (!insideSidebarMenu) {
            document.querySelectorAll(".user-menu").forEach(menu => {
                menu.classList.remove("submenu-open");
            });
        }
    });
});
