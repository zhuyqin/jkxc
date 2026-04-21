<template>
  <div class="home">
    <div class="carousel-container">
      <a-carousel 
        :autoplay="true" 
        :autoplaySpeed="4000"
        :dots="true"
        :effect="'fade'"
        :fade="true"
        class="home-carousel"
      >
        <div class="carousel-item" v-for="(image, index) in carouselImages" :key="index">
          <div class="carousel-image-wrapper">
            <img 
              :src="image" 
              :alt="`轮播图 ${index + 1}`"
              class="carousel-image"
              @error="handleImageError"
            />
          </div>
        </div>
      </a-carousel>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      carouselImages: [
        'http://101.35.140.125:9000/gongshu/order/3e20a95e458bd6377b3eb3b3db4a9a00_1769510136951.jpg',
        'http://101.35.140.125:9000/gongshu/order/138858b29cf8976986b570c577fdd000_1769510136951.jpg'
      ]
    }
  },
  methods: {
    handleImageError(e) {
      console.error('图片加载失败:', e.target.src)
      // 可以设置一个默认图片
      // e.target.src = '/default-image.jpg'
    }
  }
}
</script>

<style scoped>
  .home {
    width: 100%;
    min-height: calc(100vh - 64px);
    padding: 0;
    margin: 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .carousel-container {
    width: 100%;
    max-width: 1400px;
    margin: 0 auto;
    padding: 40px 20px;
    box-sizing: border-box;
  }

  .home-carousel {
    width: 100%;
    height: auto;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    background: #fff;
  }

  .carousel-item {
    width: 100%;
    height: 0;
    padding-bottom: 56.25%; /* 16:9 宽高比 */
    position: relative;
    overflow: hidden;
  }

  .carousel-image-wrapper {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f5f5;
  }

  .carousel-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.3s ease;
  }

  .carousel-image:hover {
    transform: scale(1.02);
  }

  /* 自定义轮播点样式 */
  .home-carousel :deep(.ant-carousel .slick-dots) {
    bottom: 20px;
  }

  .home-carousel :deep(.ant-carousel .slick-dots li) {
    width: 12px;
    height: 12px;
    margin: 0 6px;
  }

  .home-carousel :deep(.ant-carousel .slick-dots li button) {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.5);
    border: 2px solid rgba(255, 255, 255, 0.8);
  }

  .home-carousel :deep(.ant-carousel .slick-dots li.slick-active button) {
    background: #fff;
    border-color: #fff;
    width: 24px;
    border-radius: 6px;
  }

  /* 响应式设计 */
  @media (max-width: 768px) {
    .carousel-container {
      padding: 20px 10px;
    }

    .carousel-item {
      padding-bottom: 75%; /* 移动端使用 4:3 比例 */
    }
  }

  @media (max-width: 480px) {
    .carousel-container {
      padding: 10px 5px;
    }

    .home-carousel {
      border-radius: 8px;
    }
  }
</style>
