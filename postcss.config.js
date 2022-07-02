module.exports = () => {
    const plugins = {
        'postcss-import': {},
        tailwindcss: require('./tailwind.config'),
        autoprefixer: {}
    }
    return {
        plugins
    }
}